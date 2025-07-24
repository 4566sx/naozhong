package com.biblealarm.app.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.biblealarm.app.data.model.Psalm
import com.biblealarm.app.service.AudioPlaybackService
import com.biblealarm.app.service.PlaybackState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 播放管理器
 * 负责管理音频播放服务的连接和控制
 */
@Singleton
class PlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var audioService: AudioPlaybackService? = null
    private var isServiceBound = false
    
    // 播放状态
    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    // 当前播放的诗篇
    private val _currentPsalm = MutableStateFlow<Psalm?>(null)
    val currentPsalm: StateFlow<Psalm?> = _currentPsalm.asStateFlow()
    
    // 播放进度
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()
    
    // 音频时长
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()
    
    // 音量
    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    // 服务连接回调
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlaybackService.AudioPlaybackBinder
            audioService = binder.getService()
            isServiceBound = true
            
            // 同步服务状态到管理器
            audioService?.let { service ->
                observeServiceState(service)
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            isServiceBound = false
        }
    }
    
    /**
     * 绑定音频服务
     */
    fun bindAudioService() {
        if (!isServiceBound) {
            val intent = Intent(context, AudioPlaybackService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
    
    /**
     * 解绑音频服务
     */
    fun unbindAudioService() {
        if (isServiceBound) {
            context.unbindService(serviceConnection)
            isServiceBound = false
            audioService = null
        }
    }
    
    /**
     * 观察服务状态变化
     */
    private fun observeServiceState(service: AudioPlaybackService) {
        // 这里需要收集服务的StateFlow
        // 在实际实现中，可能需要使用协程来收集状态变化
    }
    
    /**
     * 播放诗篇
     */
    fun playPsalm(psalm: Psalm) {
        audioService?.let { service ->
            service.playPsalm(psalm)
            _currentPsalm.value = psalm
        } ?: run {
            // 如果服务未绑定，先绑定服务
            bindAudioService()
            // 延迟播放，等待服务连接
            _currentPsalm.value = psalm
        }
    }
    
    /**
     * 开始播放
     */
    fun startPlayback() {
        audioService?.startPlayback()
    }
    
    /**
     * 暂停播放
     */
    fun pausePlayback() {
        audioService?.pausePlayback()
        _playbackState.value = PlaybackState.PAUSED
    }
    
    /**
     * 恢复播放
     */
    fun resumePlayback() {
        audioService?.resumePlayback()
        _playbackState.value = PlaybackState.PLAYING
    }
    
    /**
     * 停止播放
     */
    fun stopPlayback() {
        audioService?.stopPlayback()
        _playbackState.value = PlaybackState.STOPPED
        _currentPosition.value = 0
    }
    
    /**
     * 切换播放/暂停状态
     */
    fun togglePlayPause() {
        when (_playbackState.value) {
            PlaybackState.PLAYING -> pausePlayback()
            PlaybackState.PAUSED -> resumePlayback()
            PlaybackState.STOPPED -> {
                _currentPsalm.value?.let { psalm ->
                    playPsalm(psalm)
                }
            }
            else -> {
                // 其他状态不处理
            }
        }
    }
    
    /**
     * 设置音量
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _volume.value = clampedVolume
        audioService?.setVolume(clampedVolume)
    }
    
    /**
     * 跳转到指定位置
     */
    fun seekTo(position: Int) {
        audioService?.seekTo(position)
        _currentPosition.value = position
    }
    
    /**
     * 播放下一篇诗篇
     */
    fun playNext() {
        audioService?.playNext()
    }
    
    /**
     * 播放上一篇诗篇
     */
    fun playPrevious() {
        audioService?.playPrevious()
    }
    
    /**
     * 获取当前播放位置
     */
    fun getCurrentPosition(): Int {
        return _currentPosition.value
    }
    
    /**
     * 获取音频总时长
     */
    fun getDuration(): Int {
        return _duration.value
    }
    
    /**
     * 获取播放进度百分比
     */
    fun getProgressPercentage(): Float {
        val duration = _duration.value
        return if (duration > 0) {
            _currentPosition.value.toFloat() / duration.toFloat()
        } else {
            0f
        }
    }
    
    /**
     * 格式化时间显示
     */
    fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
    
    /**
     * 检查是否正在播放
     */
    fun isPlaying(): Boolean {
        return _playbackState.value == PlaybackState.PLAYING
    }
    
    /**
     * 检查是否已暂停
     */
    fun isPaused(): Boolean {
        return _playbackState.value == PlaybackState.PAUSED
    }
    
    /**
     * 检查是否已停止
     */
    fun isStopped(): Boolean {
        return _playbackState.value == PlaybackState.STOPPED
    }
    
    /**
     * 检查是否有音频正在准备
     */
    fun isPreparing(): Boolean {
        return _playbackState.value == PlaybackState.PREPARING
    }
    
    /**
     * 检查是否发生错误
     */
    fun hasError(): Boolean {
        return _playbackState.value == PlaybackState.ERROR
    }
    
    /**
     * 更新播放状态（由服务调用）
     */
    fun updatePlaybackState(state: PlaybackState) {
        _playbackState.value = state
    }
    
    /**
     * 更新当前位置（由服务调用）
     */
    fun updateCurrentPosition(position: Int) {
        _currentPosition.value = position
    }
    
    /**
     * 更新音频时长（由服务调用）
     */
    fun updateDuration(duration: Int) {
        _duration.value = duration
    }
    
    /**
     * 更新当前诗篇（由服务调用）
     */
    fun updateCurrentPsalm(psalm: Psalm?) {
        _currentPsalm.value = psalm
    }
    
    /**
     * 获取播放状态描述
     */
    fun getPlaybackStateDescription(): String {
        return when (_playbackState.value) {
            PlaybackState.STOPPED -> "已停止"
            PlaybackState.PREPARING -> "准备中..."
            PlaybackState.PLAYING -> "正在播放"
            PlaybackState.PAUSED -> "已暂停"
            PlaybackState.COMPLETED -> "播放完成"
            PlaybackState.ERROR -> "播放错误"
        }
    }
    
    /**
     * 重置播放器状态
     */
    fun reset() {
        stopPlayback()
        _currentPsalm.value = null
        _currentPosition.value = 0
        _duration.value = 0
        _playbackState.value = PlaybackState.STOPPED
    }
    
    /**
     * 释放资源
     */
    fun release() {
        stopPlayback()
        unbindAudioService()
        reset()
    }
}

/**
 * 播放控制接口
 */
interface PlaybackController {
    fun play()
    fun pause()
    fun stop()
    fun next()
    fun previous()
    fun seekTo(position: Int)
    fun setVolume(volume: Float)
}

/**
 * 播放状态监听器
 */
interface PlaybackStateListener {
    fun onStateChanged(state: PlaybackState)
    fun onPsalmChanged(psalm: Psalm?)
    fun onPositionChanged(position: Int)
    fun onDurationChanged(duration: Int)
    fun onVolumeChanged(volume: Float)
    fun onError(error: String)
}