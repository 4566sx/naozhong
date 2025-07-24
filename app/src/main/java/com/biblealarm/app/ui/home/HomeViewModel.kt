package com.biblealarm.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblealarm.app.data.model.Alarm
import com.biblealarm.app.data.model.Psalm
import com.biblealarm.app.data.repository.AudioRepository
import com.biblealarm.app.manager.PsalmSelectionManager
import com.biblealarm.app.manager.PlaybackManager
import com.biblealarm.app.manager.WorkManagerScheduler
import com.biblealarm.app.service.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主屏幕ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val psalmSelectionManager: PsalmSelectionManager,
    private val playbackManager: PlaybackManager,
    private val workManagerScheduler: WorkManagerScheduler
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // 今日诗篇
    private val _todayPsalm = MutableStateFlow<Psalm?>(null)
    val todayPsalm: StateFlow<Psalm?> = _todayPsalm.asStateFlow()
    
    // 下个闹钟
    private val _nextAlarm = MutableStateFlow<Alarm?>(null)
    val nextAlarm: StateFlow<Alarm?> = _nextAlarm.asStateFlow()
    
    // 播放状态相关
    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState
    val currentPosition: StateFlow<Int> = playbackManager.currentPosition
    val duration: StateFlow<Int> = playbackManager.duration
    val volume: StateFlow<Float> = playbackManager.volume
    
    init {
        initializeApp()
        bindPlaybackService()
        scheduleDailyTasks()
    }
    
    /**
     * 初始化应用
     */
    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // 初始化诗篇数据库
                audioRepository.initializePsalms()
            } catch (e: Exception) {
                showError("初始化失败: ${e.message}")
            }
        }
    }
    
    /**
     * 绑定播放服务
     */
    private fun bindPlaybackService() {
        playbackManager.bindAudioService()
    }
    
    /**
     * 调度每日任务
     */
    private fun scheduleDailyTasks() {
        workManagerScheduler.scheduleDailyPsalmSelection()
    }
    
    /**
     * 加载今日诗篇
     */
    fun loadTodayPsalm() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingPsalm = true)
                
                val psalm = psalmSelectionManager.getTodayPsalm()
                _todayPsalm.value = psalm
                
                if (psalm == null) {
                    showError("没有可用的诗篇音频文件，请在设置中扫描音频文件夹")
                }
                
                _uiState.value = _uiState.value.copy(isLoadingPsalm = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingPsalm = false)
                showError("加载今日诗篇失败: ${e.message}")
            }
        }
    }
    
    /**
     * 刷新今日诗篇（重新选择）
     */
    fun refreshTodayPsalm() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingPsalm = true)
                
                val psalm = psalmSelectionManager.reselectTodayPsalm()
                _todayPsalm.value = psalm
                
                if (psalm != null) {
                    showMessage("已重新选择：${psalm.getDisplayTitle()}")
                } else {
                    showError("没有可用的诗篇音频文件")
                }
                
                _uiState.value = _uiState.value.copy(isLoadingPsalm = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingPsalm = false)
                showError("重新选择诗篇失败: ${e.message}")
            }
        }
    }
    
    /**
     * 加载下个闹钟
     */
    fun loadNextAlarm() {
        viewModelScope.launch {
            try {
                // 这里需要从AlarmRepository获取下个闹钟
                // 暂时设置为null，等闹钟模块完成后再实现
                _nextAlarm.value = null
            } catch (e: Exception) {
                showError("加载闹钟信息失败: ${e.message}")
            }
        }
    }
    
    /**
     * 切换播放/暂停
     */
    fun togglePlayPause() {
        val psalm = _todayPsalm.value
        if (psalm?.isAvailable == true) {
            when (playbackState.value) {
                PlaybackState.STOPPED -> {
                    playbackManager.playPsalm(psalm)
                    updatePsalmUsage(psalm.number)
                }
                PlaybackState.PLAYING -> playbackManager.pausePlayback()
                PlaybackState.PAUSED -> playbackManager.resumePlayback()
                else -> {
                    // 其他状态不处理
                }
            }
        } else {
            showError("当前诗篇音频不可用")
        }
    }
    
    /**
     * 停止播放
     */
    fun stopPlayback() {
        playbackManager.stopPlayback()
    }
    
    /**
     * 播放上一篇诗篇
     */
    fun playPrevious() {
        val currentPsalm = _todayPsalm.value
        if (currentPsalm != null) {
            viewModelScope.launch {
                val prevNumber = if (currentPsalm.number > 1) currentPsalm.number - 1 else 150
                val prevPsalm = audioRepository.getPsalmByNumber(prevNumber)
                
                if (prevPsalm?.isAvailable == true) {
                    _todayPsalm.value = prevPsalm
                    playbackManager.playPsalm(prevPsalm)
                    updatePsalmUsage(prevPsalm.number)
                } else {
                    showError("诗篇 $prevNumber 不可用")
                }
            }
        }
    }
    
    /**
     * 播放下一篇诗篇
     */
    fun playNext() {
        val currentPsalm = _todayPsalm.value
        if (currentPsalm != null) {
            viewModelScope.launch {
                val nextNumber = if (currentPsalm.number < 150) currentPsalm.number + 1 else 1
                val nextPsalm = audioRepository.getPsalmByNumber(nextNumber)
                
                if (nextPsalm?.isAvailable == true) {
                    _todayPsalm.value = nextPsalm
                    playbackManager.playPsalm(nextPsalm)
                    updatePsalmUsage(nextPsalm.number)
                } else {
                    showError("诗篇 $nextNumber 不可用")
                }
            }
        }
    }
    
    /**
     * 跳转播放位置
     */
    fun seekTo(position: Int) {
        playbackManager.seekTo(position)
    }
    
    /**
     * 设置音量
     */
    fun setVolume(volume: Float) {
        playbackManager.setVolume(volume)
    }
    
    /**
     * 更新诗篇使用记录
     */
    private fun updatePsalmUsage(psalmNumber: Int) {
        viewModelScope.launch {
            try {
                audioRepository.updatePsalmUsage(psalmNumber)
            } catch (e: Exception) {
                // 静默处理，不影响播放
            }
        }
    }
    
    /**
     * 显示错误消息
     */
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }
    
    /**
     * 显示提示消息
     */
    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 清除提示消息
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    /**
     * 获取播放进度百分比
     */
    fun getPlaybackProgress(): Float {
        return playbackManager.getProgressPercentage()
    }
    
    /**
     * 检查是否正在播放
     */
    fun isPlaying(): Boolean {
        return playbackManager.isPlaying()
    }
    
    /**
     * 检查是否已暂停
     */
    fun isPaused(): Boolean {
        return playbackManager.isPaused()
    }
    
    /**
     * 获取播放状态描述
     */
    fun getPlaybackStateDescription(): String {
        return playbackManager.getPlaybackStateDescription()
    }
    
    /**
     * 格式化时间显示
     */
    fun formatTime(milliseconds: Int): String {
        return playbackManager.formatTime(milliseconds)
    }
    
    override fun onCleared() {
        super.onCleared()
        // 解绑服务
        playbackManager.unbindAudioService()
    }
}

/**
 * 主屏幕UI状态
 */
data class HomeUiState(
    val isLoadingPsalm: Boolean = false,
    val isLoadingAlarm: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null
)