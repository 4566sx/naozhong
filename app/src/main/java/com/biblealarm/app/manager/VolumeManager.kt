package com.biblealarm.app.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 音量控制管理器
 * 负责管理应用的音量控制和音频焦点
 */
@Singleton
class VolumeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "VolumeManager"
        
        // 音量类型
        const val VOLUME_TYPE_ALARM = AudioManager.STREAM_ALARM
        const val VOLUME_TYPE_MUSIC = AudioManager.STREAM_MUSIC
        const val VOLUME_TYPE_NOTIFICATION = AudioManager.STREAM_NOTIFICATION
        
        // 默认音量设置
        const val DEFAULT_ALARM_VOLUME = 0.8f
        const val DEFAULT_MUSIC_VOLUME = 0.7f
        const val MIN_VOLUME = 0.0f
        const val MAX_VOLUME = 1.0f
    }
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    // 当前音量状态
    private val _currentVolume = MutableStateFlow(DEFAULT_ALARM_VOLUME)
    val currentVolume: StateFlow<Float> = _currentVolume.asStateFlow()
    
    // 系统音量状态
    private val _systemVolume = MutableStateFlow(getSystemVolume(VOLUME_TYPE_ALARM))
    val systemVolume: StateFlow<Float> = _systemVolume.asStateFlow()
    
    // 静音状态
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()
    
    // 音频焦点状态
    private val _hasAudioFocus = MutableStateFlow(false)
    val hasAudioFocus: StateFlow<Boolean> = _hasAudioFocus.asStateFlow()
    
    // 音量变化监听器
    private val _volumeChangeListeners = mutableSetOf<VolumeChangeListener>()
    
    // 音频焦点请求
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null
    
    init {
        initializeVolumeManager()
    }
    
    /**
     * 初始化音量管理器
     */
    private fun initializeVolumeManager() {
        // 更新系统音量状态
        updateSystemVolumeState()
        
        // 设置音频焦点监听器
        setupAudioFocusListener()
    }
    
    /**
     * 设置应用音量
     */
    fun setVolume(volume: Float, volumeType: Int = VOLUME_TYPE_ALARM) {
        val clampedVolume = volume.coerceIn(MIN_VOLUME, MAX_VOLUME)
        _currentVolume.value = clampedVolume
        
        // 通知监听器
        notifyVolumeChanged(clampedVolume, volumeType)
        
        Log.d(TAG, "设置音量: $clampedVolume")
    }
    
    /**
     * 获取当前音量
     */
    fun getCurrentVolume(): Float {
        return _currentVolume.value
    }
    
    /**
     * 增加音量
     */
    fun increaseVolume(step: Float = 0.1f, volumeType: Int = VOLUME_TYPE_ALARM) {
        val newVolume = (_currentVolume.value + step).coerceAtMost(MAX_VOLUME)
        setVolume(newVolume, volumeType)
    }
    
    /**
     * 减少音量
     */
    fun decreaseVolume(step: Float = 0.1f, volumeType: Int = VOLUME_TYPE_ALARM) {
        val newVolume = (_currentVolume.value - step).coerceAtLeast(MIN_VOLUME)
        setVolume(newVolume, volumeType)
    }
    
    /**
     * 设置静音状态
     */
    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        
        if (muted) {
            // 静音时保存当前音量，设置为0
            setVolume(MIN_VOLUME)
        } else {
            // 取消静音时恢复之前的音量
            setVolume(_currentVolume.value)
        }
        
        Log.d(TAG, "设置静音状态: $muted")
    }
    
    /**
     * 切换静音状态
     */
    fun toggleMute() {
        setMuted(!_isMuted.value)
    }
    
    /**
     * 获取系统音量
     */
    fun getSystemVolume(volumeType: Int): Float {
        val currentVolume = audioManager.getStreamVolume(volumeType)
        val maxVolume = audioManager.getStreamMaxVolume(volumeType)
        return if (maxVolume > 0) currentVolume.toFloat() / maxVolume.toFloat() else 0f
    }
    
    /**
     * 设置系统音量
     */
    fun setSystemVolume(volume: Float, volumeType: Int = VOLUME_TYPE_ALARM) {
        val clampedVolume = volume.coerceIn(MIN_VOLUME, MAX_VOLUME)
        val maxVolume = audioManager.getStreamMaxVolume(volumeType)
        val targetVolume = (clampedVolume * maxVolume).toInt()
        
        try {
            audioManager.setStreamVolume(volumeType, targetVolume, 0)
            _systemVolume.value = clampedVolume
            Log.d(TAG, "设置系统音量: $clampedVolume")
        } catch (e: SecurityException) {
            Log.e(TAG, "设置系统音量失败，权限不足", e)
        }
    }
    
    /**
     * 更新系统音量状态
     */
    private fun updateSystemVolumeState() {
        _systemVolume.value = getSystemVolume(VOLUME_TYPE_ALARM)
    }
    
    /**
     * 请求音频焦点
     */
    fun requestAudioFocus(focusGain: Int = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ 使用 AudioFocusRequest
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                
                audioFocusRequest = AudioFocusRequest.Builder(focusGain)
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener(getAudioFocusChangeListener())
                    .setAcceptsDelayedFocusGain(false)
                    .build()
                
                val result = audioManager.requestAudioFocus(audioFocusRequest!!)
                val success = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                _hasAudioFocus.value = success
                
                Log.d(TAG, "请求音频焦点: ${if (success) "成功" else "失败"}")
                success
            } else {
                // Android 8.0以下使用旧API
                val result = audioManager.requestAudioFocus(
                    getAudioFocusChangeListener(),
                    AudioManager.STREAM_ALARM,
                    focusGain
                )
                val success = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                _hasAudioFocus.value = success
                
                Log.d(TAG, "请求音频焦点(旧API): ${if (success) "成功" else "失败"}")
                success
            }
        } catch (e: Exception) {
            Log.e(TAG, "请求音频焦点失败", e)
            false
        }
    }
    
    /**
     * 释放音频焦点
     */
    fun releaseAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { request ->
                    audioManager.abandonAudioFocusRequest(request)
                    audioFocusRequest = null
                }
            } else {
                audioFocusChangeListener?.let { listener ->
                    audioManager.abandonAudioFocus(listener)
                }
            }
            
            _hasAudioFocus.value = false
            Log.d(TAG, "释放音频焦点")
        } catch (e: Exception) {
            Log.e(TAG, "释放音频焦点失败", e)
        }
    }
    
    /**
     * 设置音频焦点监听器
     */
    private fun setupAudioFocusListener() {
        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            handleAudioFocusChange(focusChange)
        }
    }
    
    /**
     * 获取音频焦点变化监听器
     */
    private fun getAudioFocusChangeListener(): AudioManager.OnAudioFocusChangeListener {
        return audioFocusChangeListener ?: AudioManager.OnAudioFocusChangeListener { focusChange ->
            handleAudioFocusChange(focusChange)
        }
    }
    
    /**
     * 处理音频焦点变化
     */
    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // 获得音频焦点
                _hasAudioFocus.value = true
                if (_isMuted.value) {
                    setMuted(false)
                }
                Log.d(TAG, "获得音频焦点")
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // 永久失去音频焦点
                _hasAudioFocus.value = false
                notifyAudioFocusLost(permanent = true)
                Log.d(TAG, "永久失去音频焦点")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // 暂时失去音频焦点
                _hasAudioFocus.value = false
                notifyAudioFocusLost(permanent = false)
                Log.d(TAG, "暂时失去音频焦点")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // 失去音频焦点但可以降低音量继续播放
                _hasAudioFocus.value = false
                notifyAudioFocusDuck()
                Log.d(TAG, "失去音频焦点，降低音量")
            }
        }
    }
    
    /**
     * 检查是否有音频焦点
     */
    fun hasAudioFocus(): Boolean {
        return _hasAudioFocus.value
    }
    
    /**
     * 获取音量百分比显示
     */
    fun getVolumePercentage(): Int {
        return (_currentVolume.value * 100).toInt()
    }
    
    /**
     * 从百分比设置音量
     */
    fun setVolumeFromPercentage(percentage: Int, volumeType: Int = VOLUME_TYPE_ALARM) {
        val volume = (percentage.coerceIn(0, 100) / 100f)
        setVolume(volume, volumeType)
    }
    
    /**
     * 检查系统是否静音
     */
    fun isSystemMuted(volumeType: Int = VOLUME_TYPE_ALARM): Boolean {
        return getSystemVolume(volumeType) == 0f
    }
    
    /**
     * 获取音量描述文本
     */
    fun getVolumeDescription(): String {
        val percentage = getVolumePercentage()
        return when {
            percentage == 0 -> "静音"
            percentage <= 25 -> "低音量"
            percentage <= 50 -> "中等音量"
            percentage <= 75 -> "较高音量"
            else -> "最大音量"
        }
    }
    
    /**
     * 添加音量变化监听器
     */
    fun addVolumeChangeListener(listener: VolumeChangeListener) {
        _volumeChangeListeners.add(listener)
    }
    
    /**
     * 移除音量变化监听器
     */
    fun removeVolumeChangeListener(listener: VolumeChangeListener) {
        _volumeChangeListeners.remove(listener)
    }
    
    /**
     * 通知音量变化
     */
    private fun notifyVolumeChanged(volume: Float, volumeType: Int) {
        _volumeChangeListeners.forEach { listener ->
            try {
                listener.onVolumeChanged(volume, volumeType)
            } catch (e: Exception) {
                Log.e(TAG, "通知音量变化失败", e)
            }
        }
    }
    
    /**
     * 通知音频焦点丢失
     */
    private fun notifyAudioFocusLost(permanent: Boolean) {
        _volumeChangeListeners.forEach { listener ->
            try {
                listener.onAudioFocusLost(permanent)
            } catch (e: Exception) {
                Log.e(TAG, "通知音频焦点丢失失败", e)
            }
        }
    }
    
    /**
     * 通知音频焦点降低音量
     */
    private fun notifyAudioFocusDuck() {
        _volumeChangeListeners.forEach { listener ->
            try {
                listener.onAudioFocusDuck()
            } catch (e: Exception) {
                Log.e(TAG, "通知音频焦点降低音量失败", e)
            }
        }
    }
    
    /**
     * 保存音量设置
     */
    fun saveVolumeSettings() {
        val prefs = context.getSharedPreferences("volume_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat("current_volume", _currentVolume.value)
            .putBoolean("is_muted", _isMuted.value)
            .apply()
        
        Log.d(TAG, "保存音量设置")
    }
    
    /**
     * 加载音量设置
     */
    fun loadVolumeSettings() {
        val prefs = context.getSharedPreferences("volume_prefs", Context.MODE_PRIVATE)
        val savedVolume = prefs.getFloat("current_volume", DEFAULT_ALARM_VOLUME)
        val savedMuted = prefs.getBoolean("is_muted", false)
        
        _currentVolume.value = savedVolume
        _isMuted.value = savedMuted
        
        Log.d(TAG, "加载音量设置: 音量=$savedVolume, 静音=$savedMuted")
    }
    
    /**
     * 重置音量设置
     */
    fun resetVolumeSettings() {
        _currentVolume.value = DEFAULT_ALARM_VOLUME
        _isMuted.value = false
        saveVolumeSettings()
        
        Log.d(TAG, "重置音量设置")
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        releaseAudioFocus()
        _volumeChangeListeners.clear()
        Log.d(TAG, "清理音量管理器资源")
    }
}

/**
 * 音量变化监听器接口
 */
interface VolumeChangeListener {
    fun onVolumeChanged(volume: Float, volumeType: Int)
    fun onAudioFocusLost(permanent: Boolean)
    fun onAudioFocusDuck()
}

/**
 * 音量控制结果
 */
sealed class VolumeControlResult {
    object Success : VolumeControlResult()
    data class Error(val message: String) : VolumeControlResult()
    object PermissionRequired : VolumeControlResult()
}

/**
 * 音量配置数据类
 */
data class VolumeConfig(
    val alarmVolume: Float = VolumeManager.DEFAULT_ALARM_VOLUME,
    val musicVolume: Float = VolumeManager.DEFAULT_MUSIC_VOLUME,
    val isMuted: Boolean = false,
    val useSystemVolume: Boolean = true,
    val volumeStep: Float = 0.1f
)