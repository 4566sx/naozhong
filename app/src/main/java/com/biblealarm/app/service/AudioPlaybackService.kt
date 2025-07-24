package com.biblealarm.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.biblealarm.app.MainActivity
import com.biblealarm.app.R
import com.biblealarm.app.data.model.Psalm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * 音频播放服务
 */
class AudioPlaybackService : Service(), MediaPlayer.OnPreparedListener, 
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "audio_playback_channel"
        
        // 播放控制动作
        const val ACTION_PLAY = "com.biblealarm.app.ACTION_PLAY"
        const val ACTION_PAUSE = "com.biblealarm.app.ACTION_PAUSE"
        const val ACTION_STOP = "com.biblealarm.app.ACTION_STOP"
        const val ACTION_NEXT = "com.biblealarm.app.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.biblealarm.app.ACTION_PREVIOUS"
    }
    
    private val binder = AudioPlaybackBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    // 播放状态
    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    val playbackState: StateFlow<PlaybackState> = _playbackState
    
    private val _currentPsalm = MutableStateFlow<Psalm?>(null)
    val currentPsalm: StateFlow<Psalm?> = _currentPsalm
    
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition
    
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration
    
    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume
    
    // 音频焦点监听器
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // 重新获得音频焦点，恢复播放
                if (_playbackState.value == PlaybackState.PAUSED) {
                    resumePlayback()
                }
                setVolume(_volume.value)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // 永久失去音频焦点，停止播放
                stopPlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // 暂时失去音频焦点，暂停播放
                pausePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // 降低音量
                setVolume(_volume.value * 0.3f)
            }
        }
    }
    
    inner class AudioPlaybackBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }
    
    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> resumePlayback()
            ACTION_PAUSE -> pausePlayback()
            ACTION_STOP -> stopPlayback()
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
        }
        return START_NOT_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        abandonAudioFocus()
    }
    
    /**
     * 播放诗篇
     */
    fun playPsalm(psalm: Psalm) {
        serviceScope.launch {
            try {
                if (!File(psalm.audioFilePath).exists()) {
                    _playbackState.value = PlaybackState.ERROR
                    return@launch
                }
                
                _currentPsalm.value = psalm
                
                // 请求音频焦点
                if (!requestAudioFocus()) {
                    return@launch
                }
                
                // 准备MediaPlayer
                prepareMediaPlayer(psalm.audioFilePath)
                
            } catch (e: Exception) {
                _playbackState.value = PlaybackState.ERROR
            }
        }
    }
    
    /**
     * 准备MediaPlayer
     */
    private fun prepareMediaPlayer(audioPath: String) {
        releaseMediaPlayer()
        
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            
            setOnPreparedListener(this@AudioPlaybackService)
            setOnCompletionListener(this@AudioPlaybackService)
            setOnErrorListener(this@AudioPlaybackService)
            
            try {
                setDataSource(audioPath)
                prepareAsync()
                _playbackState.value = PlaybackState.PREPARING
            } catch (e: Exception) {
                _playbackState.value = PlaybackState.ERROR
            }
        }
    }
    
    /**
     * 开始播放
     */
    fun startPlayback() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                _playbackState.value = PlaybackState.PLAYING
                startForeground(NOTIFICATION_ID, createNotification())
                startPositionUpdates()
            }
        }
    }
    
    /**
     * 暂停播放
     */
    fun pausePlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = PlaybackState.PAUSED
                updateNotification()
            }
        }
    }
    
    /**
     * 恢复播放
     */
    fun resumePlayback() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying && _playbackState.value == PlaybackState.PAUSED) {
                player.start()
                _playbackState.value = PlaybackState.PLAYING
                updateNotification()
                startPositionUpdates()
            }
        }
    }
    
    /**
     * 停止播放
     */
    fun stopPlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
        }
        _playbackState.value = PlaybackState.STOPPED
        _currentPosition.value = 0
        stopForeground(true)
        abandonAudioFocus()
    }
    
    /**
     * 设置音量
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _volume.value = clampedVolume
        mediaPlayer?.setVolume(clampedVolume, clampedVolume)
    }
    
    /**
     * 跳转到指定位置
     */
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _currentPosition.value = position
    }
    
    /**
     * 播放下一篇
     */
    fun playNext() {
        _currentPsalm.value?.let { current ->
            val nextNumber = if (current.number < 150) current.number + 1 else 1
            // 这里需要从Repository获取下一篇诗篇
            // 实际实现中需要注入AudioRepository
        }
    }
    
    /**
     * 播放上一篇
     */
    fun playPrevious() {
        _currentPsalm.value?.let { current ->
            val prevNumber = if (current.number > 1) current.number - 1 else 150
            // 这里需要从Repository获取上一篇诗篇
            // 实际实现中需要注入AudioRepository
        }
    }
    
    /**
     * MediaPlayer准备完成回调
     */
    override fun onPrepared(mp: MediaPlayer?) {
        mp?.let { player ->
            _duration.value = player.duration
            setVolume(_volume.value)
            startPlayback()
        }
    }
    
    /**
     * MediaPlayer播放完成回调
     */
    override fun onCompletion(mp: MediaPlayer?) {
        _playbackState.value = PlaybackState.COMPLETED
        _currentPosition.value = 0
        stopForeground(true)
    }
    
    /**
     * MediaPlayer错误回调
     */
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        _playbackState.value = PlaybackState.ERROR
        return true
    }
    
    /**
     * 请求音频焦点
     */
    private fun requestAudioFocus(): Boolean {
        audioManager?.let { manager ->
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
                
                manager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                manager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        }
        return false
    }
    
    /**
     * 放弃音频焦点
     */
    private fun abandonAudioFocus() {
        audioManager?.let { manager ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { request ->
                    manager.abandonAudioFocusRequest(request)
                }
            } else {
                @Suppress("DEPRECATION")
                manager.abandonAudioFocus(audioFocusChangeListener)
            }
        }
    }
    
    /**
     * 释放MediaPlayer资源
     */
    private fun releaseMediaPlayer() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
    }
    
    /**
     * 开始位置更新
     */
    private fun startPositionUpdates() {
        serviceScope.launch {
            while (_playbackState.value == PlaybackState.PLAYING) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition
                    }
                }
                kotlinx.coroutines.delay(1000) // 每秒更新一次
            }
        }
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "音频播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "圣经闹钟音频播放通知"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建通知
     */
    private fun createNotification(): Notification {
        val psalm = _currentPsalm.value
        val isPlaying = _playbackState.value == PlaybackState.PLAYING
        
        val playPauseIntent = Intent(this, AudioPlaybackService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, AudioPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(psalm?.getDisplayTitle() ?: "圣经闹钟")
            .setContentText(if (isPlaying) "正在播放" else "已暂停")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentPendingIntent)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "暂停" else "播放",
                playPausePendingIntent
            )
            .addAction(R.drawable.ic_stop, "停止", stopPendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1))
            .setOngoing(isPlaying)
            .build()
    }
    
    /**
     * 更新通知
     */
    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
}

/**
 * 播放状态枚举
 */
enum class PlaybackState {
    STOPPED,    // 停止
    PREPARING,  // 准备中
    PLAYING,    // 播放中
    PAUSED,     // 暂停
    COMPLETED,  // 播放完成
    ERROR       // 错误
}