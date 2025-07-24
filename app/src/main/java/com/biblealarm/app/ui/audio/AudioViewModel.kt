package com.biblealarm.app.ui.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblealarm.app.data.model.Psalm
import com.biblealarm.app.data.repository.AudioRepository
import com.biblealarm.app.data.repository.ScanResult
import com.biblealarm.app.data.repository.ValidationResult
import com.biblealarm.app.service.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 音频管理ViewModel
 */
@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioRepository: AudioRepository
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()
    
    // 诗篇列表
    private val _psalms = MutableStateFlow<List<Psalm>>(emptyList())
    val psalms: StateFlow<List<Psalm>> = _psalms.asStateFlow()
    
    // 可用诗篇列表
    private val _availablePsalms = MutableStateFlow<List<Psalm>>(emptyList())
    val availablePsalms: StateFlow<List<Psalm>> = _availablePsalms.asStateFlow()
    
    // 当前播放的诗篇
    private val _currentPsalm = MutableStateFlow<Psalm?>(null)
    val currentPsalm: StateFlow<Psalm?> = _currentPsalm.asStateFlow()
    
    // 播放状态
    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    // 音量
    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    // 播放进度
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()
    
    init {
        initializeData()
        observePsalms()
    }
    
    /**
     * 初始化数据
     */
    private fun initializeData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // 初始化诗篇数据库
                audioRepository.initializePsalms()
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "初始化失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 观察诗篇数据变化
     */
    private fun observePsalms() {
        viewModelScope.launch {
            audioRepository.getAllPsalms().collect { psalms ->
                _psalms.value = psalms
            }
        }
        
        viewModelScope.launch {
            audioRepository.getAvailablePsalms().collect { availablePsalms ->
                _availablePsalms.value = availablePsalms
            }
        }
    }
    
    /**
     * 扫描音频文件夹
     */
    fun scanAudioFolder(folderPath: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isScanning = true,
                    scanMessage = "正在扫描音频文件..."
                )
                
                val result = audioRepository.scanAudioFolder(folderPath)
                
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    scanMessage = result.message,
                    lastScanResult = result
                )
                
                if (result.success) {
                    showMessage("扫描完成：找到 ${result.foundFiles} 个文件，更新了 ${result.updatedPsalms} 篇诗篇")
                } else {
                    showError(result.message)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    scanMessage = "扫描失败: ${e.message}"
                )
                showError("扫描失败: ${e.message}")
            }
        }
    }
    
    /**
     * 验证音频文件
     */
    fun validateAudioFiles() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isValidating = true)
                
                val result = audioRepository.validateAudioFiles()
                
                _uiState.value = _uiState.value.copy(
                    isValidating = false,
                    lastValidationResult = result
                )
                
                val message = "验证完成：有效 ${result.validCount} 个，无效 ${result.invalidCount} 个"
                showMessage(message)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isValidating = false)
                showError("验证失败: ${e.message}")
            }
        }
    }
    
    /**
     * 获取随机诗篇
     */
    fun getRandomPsalm() {
        viewModelScope.launch {
            try {
                val randomPsalm = audioRepository.getRandomPsalm()
                if (randomPsalm != null) {
                    _currentPsalm.value = randomPsalm
                    showMessage("已选择：${randomPsalm.getDisplayTitle()}")
                } else {
                    showError("没有可用的诗篇音频文件")
                }
            } catch (e: Exception) {
                showError("获取随机诗篇失败: ${e.message}")
            }
        }
    }
    
    /**
     * 选择指定诗篇
     */
    fun selectPsalm(psalmNumber: Int) {
        viewModelScope.launch {
            try {
                val psalm = audioRepository.getPsalmByNumber(psalmNumber)
                if (psalm != null && psalm.isAvailable) {
                    _currentPsalm.value = psalm
                    showMessage("已选择：${psalm.getDisplayTitle()}")
                } else {
                    showError("诗篇 $psalmNumber 不可用")
                }
            } catch (e: Exception) {
                showError("选择诗篇失败: ${e.message}")
            }
        }
    }
    
    /**
     * 播放当前诗篇
     */
    fun playCurrentPsalm() {
        val psalm = _currentPsalm.value
        if (psalm != null && psalm.isAvailable) {
            // 这里需要与AudioPlaybackService交互
            // 实际实现中需要通过ServiceConnection或其他方式
            _playbackState.value = PlaybackState.PLAYING
            
            // 更新使用记录
            viewModelScope.launch {
                audioRepository.updatePsalmUsage(psalm.number)
            }
        } else {
            showError("没有可播放的诗篇")
        }
    }
    
    /**
     * 暂停播放
     */
    fun pausePlayback() {
        _playbackState.value = PlaybackState.PAUSED
    }
    
    /**
     * 停止播放
     */
    fun stopPlayback() {
        _playbackState.value = PlaybackState.STOPPED
        _currentPosition.value = 0
    }
    
    /**
     * 设置音量
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _volume.value = clampedVolume
        // 这里需要通知AudioPlaybackService更新音量
    }
    
    /**
     * 跳转播放位置
     */
    fun seekTo(position: Int) {
        _currentPosition.value = position
        // 这里需要通知AudioPlaybackService跳转位置
    }
    
    /**
     * 播放下一篇诗篇
     */
    fun playNext() {
        val current = _currentPsalm.value
        if (current != null) {
            val nextNumber = if (current.number < 150) current.number + 1 else 1
            selectPsalm(nextNumber)
        }
    }
    
    /**
     * 播放上一篇诗篇
     */
    fun playPrevious() {
        val current = _currentPsalm.value
        if (current != null) {
            val prevNumber = if (current.number > 1) current.number - 1 else 150
            selectPsalm(prevNumber)
        }
    }
    
    /**
     * 获取默认音频文件夹路径
     */
    fun getDefaultAudioFolder(): String {
        return audioRepository.getDefaultAudioFolder()
    }
    
    /**
     * 创建默认音频文件夹
     */
    fun createDefaultAudioFolder() {
        viewModelScope.launch {
            try {
                val success = audioRepository.createDefaultAudioFolder()
                if (success) {
                    showMessage("已创建默认音频文件夹")
                } else {
                    showError("创建默认音频文件夹失败")
                }
            } catch (e: Exception) {
                showError("创建文件夹失败: ${e.message}")
            }
        }
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
}

/**
 * 音频UI状态数据类
 */
data class AudioUiState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val isValidating: Boolean = false,
    val scanMessage: String = "",
    val errorMessage: String? = null,
    val message: String? = null,
    val lastScanResult: ScanResult? = null,
    val lastValidationResult: ValidationResult? = null
)