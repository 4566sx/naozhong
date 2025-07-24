package com.biblealarm.app.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblealarm.app.data.model.Alarm
import com.biblealarm.app.data.model.Psalm
import com.biblealarm.app.data.repository.AlarmRepository
import com.biblealarm.app.data.repository.AudioRepository
import com.biblealarm.app.manager.PlaybackManager
import com.biblealarm.app.manager.SnoozeManager
import com.biblealarm.app.manager.VolumeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * 闹钟活动ViewModel
 */
@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val audioRepository: AudioRepository,
    private val playbackManager: PlaybackManager,
    private val snoozeManager: SnoozeManager,
    private val volumeManager: VolumeManager
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()
    
    // 当前时间
    private val _currentTime = MutableStateFlow(getCurrentTimeString())
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()
    
    private var alarmId: Long = -1L
    private var psalmNumber: Int = -1
    
    /**
     * 初始化
     */
    fun initialize(alarmId: Long, psalmNumber: Int) {
        this.alarmId = alarmId
        this.psalmNumber = psalmNumber
        
        viewModelScope.launch {
            try {
                // 加载闹钟信息
                val alarm = alarmRepository.getAlarmById(alarmId)
                if (alarm == null) {
                    showError("找不到指定的闹钟")
                    return@launch
                }
                
                // 加载诗篇信息
                val psalm = if (psalmNumber > 0) {
                    audioRepository.getPsalmByNumber(psalmNumber)
                } else {
                    audioRepository.getRandomPsalm()
                }
                
                if (psalm == null) {
                    showError("找不到可用的诗篇音频")
                    return@launch
                }
                
                // 更新UI状态
                _uiState.value = _uiState.value.copy(
                    alarm = alarm,
                    psalm = psalm,
                    isLoading = false
                )
                
                // 开始播放音频
                startPlayback(psalm)
                
            } catch (e: Exception) {
                showError("初始化闹钟失败: ${e.message}")
            }
        }
    }
    
    /**
     * 开始播放音频
     */
    private fun startPlayback(psalm: Psalm) {
        viewModelScope.launch {
            try {
                // 请求音频焦点
                val audioFocusGranted = volumeManager.requestAudioFocus()
                if (!audioFocusGranted) {
                    showError("无法获取音频焦点")
                    return@launch
                }
                
                // 设置音量
                volumeManager.setVolume(0.8f) // 闹钟音量设置为80%
                
                // 开始播放
                playbackManager.playPsalm(psalm)
                
                _uiState.value = _uiState.value.copy(isPlaying = true)
                
            } catch (e: Exception) {
                showError("播放音频失败: ${e.message}")
            }
        }
    }
    
    /**
     * 贪睡闹钟
     */
    fun snoozeAlarm() {
        val alarm = _uiState.value.alarm ?: return
        
        viewModelScope.launch {
            try {
                val result = snoozeManager.snoozeAlarm(alarm)
                when (result) {
                    is com.biblealarm.app.manager.SnoozeResult.Success -> {
                        // 贪睡设置成功，停止当前播放
                        stopPlayback()
                    }
                    is com.biblealarm.app.manager.SnoozeResult.Error -> {
                        showError(result.message)
                    }
                }
            } catch (e: Exception) {
                showError("设置贪睡失败: ${e.message}")
            }
        }
    }
    
    /**
     * 停止闹钟
     */
    fun stopAlarm() {
        viewModelScope.launch {
            try {
                // 停止播放
                stopPlayback()
                
                // 清除贪睡状态
                if (alarmId != -1L) {
                    snoozeManager.clearSnoozeState(alarmId)
                }
                
                // 如果是一次性闹钟，禁用它
                val alarm = _uiState.value.alarm
                if (alarm != null && alarm.repeatDays.isEmpty()) {
                    alarmRepository.setAlarmEnabled(alarm.id, false)
                }
                
            } catch (e: Exception) {
                showError("停止闹钟失败: ${e.message}")
            }
        }
    }
    
    /**
     * 停止播放
     */
    private fun stopPlayback() {
        try {
            playbackManager.stopPlayback()
            volumeManager.releaseAudioFocus()
            
            _uiState.value = _uiState.value.copy(isPlaying = false)
            
        } catch (e: Exception) {
            showError("停止播放失败: ${e.message}")
        }
    }
    
    /**
     * 更新当前时间
     */
    fun updateCurrentTime() {
        _currentTime.value = getCurrentTimeString()
    }
    
    /**
     * 获取当前时间字符串
     */
    private fun getCurrentTimeString(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }
    
    /**
     * 显示错误消息
     */
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            isLoading = false
        )
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 获取闹钟信息
     */
    fun getAlarmInfo(): String {
        val alarm = _uiState.value.alarm
        val psalm = _uiState.value.psalm
        
        return buildString {
            if (alarm != null) {
                append("闹钟: ${alarm.label}\n")
                append("时间: ${alarm.getTimeString()}\n")
                if (alarm.repeatDays.isNotEmpty()) {
                    append("重复: ${alarm.getRepeatText()}\n")
                }
            }
            
            if (psalm != null) {
                append("诗篇: ${psalm.getDisplayTitle()}")
            }
        }
    }
    
    /**
     * 检查是否可以贪睡
     */
    fun canSnooze(): Boolean {
        return _uiState.value.alarm?.isSnoozeEnabled == true
    }
    
    /**
     * 获取贪睡时长
     */
    fun getSnoozeDuration(): Int {
        return _uiState.value.alarm?.snoozeDuration ?: 5
    }
    
    /**
     * 检查是否正在播放
     */
    fun isPlaying(): Boolean {
        return _uiState.value.isPlaying
    }
    
    /**
     * 获取播放状态描述
     */
    fun getPlaybackStatusDescription(): String {
        return when {
            _uiState.value.isPlaying -> "正在播放"
            _uiState.value.psalm?.isAvailable == false -> "音频不可用"
            _uiState.value.isLoading -> "加载中..."
            else -> "已停止"
        }
    }
    
    /**
     * 切换播放状态
     */
    fun togglePlayback() {
        val psalm = _uiState.value.psalm ?: return
        
        if (_uiState.value.isPlaying) {
            playbackManager.pausePlayback()
            _uiState.value = _uiState.value.copy(isPlaying = false)
        } else {
            playbackManager.resumePlayback()
            _uiState.value = _uiState.value.copy(isPlaying = true)
        }
    }
    
    /**
     * 调整音量
     */
    fun adjustVolume(volume: Float) {
        volumeManager.setVolume(volume.coerceIn(0f, 1f))
    }
    
    /**
     * 获取当前音量
     */
    fun getCurrentVolume(): Float {
        return volumeManager.getCurrentVolume()
    }
    
    /**
     * 静音/取消静音
     */
    fun toggleMute() {
        volumeManager.toggleMute()
    }
    
    /**
     * 检查是否静音
     */
    fun isMuted(): Boolean {
        return volumeManager.isMuted.value
    }
    
    /**
     * 获取闹钟统计信息
     */
    fun getAlarmStats(): String {
        val alarm = _uiState.value.alarm ?: return ""
        val snoozeStats = snoozeManager.getSnoozeStats(alarm.id)
        
        return buildString {
            append("贪睡次数: ${snoozeStats.currentSnoozeCount}\n")
            append("历史贪睡: ${snoozeStats.totalSnoozeCount}次\n")
            if (snoozeStats.isCurrentlySnoozed) {
                append("状态: 贪睡中")
            } else {
                append("状态: 正常")
            }
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        try {
            stopPlayback()
            volumeManager.cleanup()
        } catch (e: Exception) {
            // 忽略清理时的错误
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}

/**
 * 闹钟UI状态
 */
data class AlarmUiState(
    val alarm: Alarm? = null,
    val psalm: Psalm? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val errorMessage: String? = null
)