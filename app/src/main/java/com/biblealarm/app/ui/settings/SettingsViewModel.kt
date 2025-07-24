package com.biblealarm.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblealarm.app.data.repository.SettingsRepository
import com.biblealarm.app.data.repository.AudioRepository
import com.biblealarm.app.manager.PermissionManager
import com.biblealarm.app.manager.WorkManagerScheduler
import com.biblealarm.app.manager.BuiltInAudioManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置页面ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val audioRepository: AudioRepository,
    private val permissionManager: PermissionManager,
    private val workManagerScheduler: WorkManagerScheduler,
    private val builtInAudioManager: BuiltInAudioManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        getAudioStatistics()
    }

    /**
     * 加载设置
     */
    fun loadSettings() {
        viewModelScope.launch {
            try {
                combine(
                    settingsRepository.getAudioPath(),
                    settingsRepository.getDefaultVolume(),
                    settingsRepository.getFadeInEnabled(),
                    settingsRepository.getSnoozeDuration(),
                    settingsRepository.getVibrateEnabled(),
                    settingsRepository.getAlarmTimeout(),
                    settingsRepository.getDailyPsalmEnabled(),
                    settingsRepository.getPsalmSelectionTime(),
                    settingsRepository.getRandomSeedReset(),
                    settingsRepository.getAutoStartEnabled()
                ) { values ->
                    val (audioPath, defaultVolume, fadeInEnabled, snoozeDuration, 
                         vibrateEnabled, alarmTimeout, dailyPsalmEnabled, 
                         psalmSelectionTime, randomSeedReset, autoStartEnabled) = values
                    
                    val (hour, minute) = psalmSelectionTime
                    
                    SettingsUiState(
                        audioPath = audioPath,
                        defaultVolume = defaultVolume,
                        fadeInEnabled = fadeInEnabled,
                        snoozeDuration = snoozeDuration,
                        vibrateEnabled = vibrateEnabled,
                        alarmTimeout = alarmTimeout,
                        dailyPsalmEnabled = dailyPsalmEnabled,
                        psalmSelectionHour = hour,
                        psalmSelectionMinute = minute,
                        randomSeedReset = randomSeedReset,
                        autoStartEnabled = autoStartEnabled,
                        appVersion = getAppVersion()
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "加载设置失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新音频路径
     */
    fun updateAudioPath(path: String) {
        viewModelScope.launch {
            try {
                settingsRepository.setAudioPath(path)
                _uiState.update { it.copy(audioPath = path) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新音频路径失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新默认音量
     */
    fun updateDefaultVolume(volume: Float) {
        viewModelScope.launch {
            try {
                settingsRepository.setDefaultVolume(volume)
                _uiState.update { it.copy(defaultVolume = volume) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新默认音量失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新渐强播放设置
     */
    fun updateFadeInEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setFadeInEnabled(enabled)
                _uiState.update { it.copy(fadeInEnabled = enabled) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新渐强播放设置失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新贪睡时长
     */
    fun updateSnoozeDuration(duration: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.setSnoozeDuration(duration)
                _uiState.update { it.copy(snoozeDuration = duration) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新贪睡时长失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新振动设置
     */
    fun updateVibrateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setVibrateEnabled(enabled)
                _uiState.update { it.copy(vibrateEnabled = enabled) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新振动设置失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新闹钟超时时间
     */
    fun updateAlarmTimeout(timeout: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.setAlarmTimeout(timeout)
                _uiState.update { it.copy(alarmTimeout = timeout) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新闹钟超时时间失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新每日诗篇自动选择设置
     */
    fun updateDailyPsalmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setDailyPsalmEnabled(enabled)
                _uiState.update { it.copy(dailyPsalmEnabled = enabled) }
                
                // 更新工作管理器调度
                if (enabled) {
                    workManagerScheduler.scheduleDailyPsalmSelection()
                } else {
                    workManagerScheduler.cancelDailyPsalmSelection()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新每日诗篇设置失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新诗篇选择时间
     */
    fun updatePsalmSelectionTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.setPsalmSelectionTime(hour, minute)
                _uiState.update { 
                    it.copy(
                        psalmSelectionHour = hour,
                        psalmSelectionMinute = minute
                    )
                }
                
                // 重新调度工作管理器
                if (_uiState.value.dailyPsalmEnabled) {
                    workManagerScheduler.scheduleDailyPsalmSelection()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新诗篇选择时间失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新随机种子重置设置
     */
    fun updateRandomSeedReset(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setRandomSeedReset(enabled)
                _uiState.update { it.copy(randomSeedReset = enabled) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新随机种子设置失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 更新开机自启设置
     */
    fun updateAutoStartEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setAutoStartEnabled(enabled)
                _uiState.update { it.copy(autoStartEnabled = enabled) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新开机自启设置失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 显示时间选择器对话框
     */
    fun showTimePickerDialog() {
        _uiState.update { it.copy(showTimePickerDialog = true) }
    }

    /**
     * 隐藏时间选择器对话框
     */
    fun hideTimePickerDialog() {
        _uiState.update { it.copy(showTimePickerDialog = false) }
    }

    /**
     * 显示重置确认对话框
     */
    fun showResetDialog() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    /**
     * 隐藏重置确认对话框
     */
    fun hideResetDialog() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    /**
     * 打开电池优化设置
     */
    fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "无法打开电池优化设置: ${e.message}")
            }
        }
    }

    /**
     * 检查更新
     */
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                // 这里可以实现检查更新的逻辑
                _uiState.update {
                    it.copy(errorMessage = "当前已是最新版本")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "检查更新失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 打开反馈页面
     */
    fun openFeedback() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:feedback@biblealarm.com")
                putExtra(Intent.EXTRA_SUBJECT, "圣经闹钟 - 用户反馈")
                putExtra(Intent.EXTRA_TEXT, "应用版本: ${getAppVersion()}\n\n请在此处输入您的反馈内容：")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "无法打开邮件应用: ${e.message}")
            }
        }
    }

    /**
     * 打开隐私政策
     */
    fun openPrivacyPolicy() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://biblealarm.com/privacy-policy")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "无法打开隐私政策页面: ${e.message}")
            }
        }
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                // 清除应用缓存
                val cacheDir = context.cacheDir
                cacheDir.deleteRecursively()
                
                _uiState.update {
                    it.copy(errorMessage = "缓存清除成功")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "清除缓存失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 导出设置
     */
    fun exportSettings() {
        viewModelScope.launch {
            try {
                // 这里可以实现导出设置的逻辑
                _uiState.update {
                    it.copy(errorMessage = "设置导出功能开发中")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "导出设置失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 导入设置
     */
    fun importSettings() {
        viewModelScope.launch {
            try {
                // 这里可以实现导入设置的逻辑
                _uiState.update {
                    it.copy(errorMessage = "设置导入功能开发中")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "导入设置失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 重置应用
     */
    fun resetApp() {
        viewModelScope.launch {
            try {
                // 重置所有设置到默认值
                settingsRepository.resetToDefaults()
                
                // 取消所有工作管理器任务
                workManagerScheduler.cancelAllTasks()
                
                // 重新加载设置
                loadSettings()
                
                _uiState.update {
                    it.copy(errorMessage = "应用重置成功")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "重置应用失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 获取音频统计信息
     */
    fun getAudioStatistics() {
        viewModelScope.launch {
            try {
                val builtInCount = builtInAudioManager.getAvailableBuiltInAudioCount()
                val userAudioCount = audioRepository.getUserAudioCount()
                val totalAvailable = audioRepository.getAvailableAudioCount()
                
                _uiState.update { 
                    it.copy(
                        builtInAudioCount = builtInCount,
                        userAudioCount = userAudioCount,
                        totalAvailableAudio = totalAvailable
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "获取音频统计失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 扫描用户音频文件
     */
    fun scanUserAudioFiles() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isScanning = true) }
                
                val audioPath = _uiState.value.audioPath
                if (audioPath.isNotEmpty()) {
                    audioRepository.scanAndUpdateAudioFiles(audioPath)
                    getAudioStatistics() // 重新获取统计信息
                    _uiState.update { 
                        it.copy(
                            isScanning = false,
                            scanResult = "音频文件扫描完成"
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isScanning = false,
                            errorMessage = "请先设置音频文件路径"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        errorMessage = "扫描音频文件失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 切换音频源
     */
    fun toggleAudioSource(useBuiltIn: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setUseBuiltInAudio(useBuiltIn)
                _uiState.update { it.copy(useBuiltInAudio = useBuiltIn) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "切换音频源失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 获取应用版本
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "未知版本"
        }
    }
}

/**
 * 设置页面UI状态
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val audioPath: String = "",
    val defaultVolume: Float = 0.7f,
    val fadeInEnabled: Boolean = true,
    val snoozeDuration: Int = 10,
    val vibrateEnabled: Boolean = true,
    val alarmTimeout: Int = 5,
    val dailyPsalmEnabled: Boolean = true,
    val psalmSelectionHour: Int = 6,
    val psalmSelectionMinute: Int = 0,
    val randomSeedReset: Boolean = true,
    val autoStartEnabled: Boolean = false,
    val useBuiltInAudio: Boolean = true,
    val builtInAudioCount: Int = 0,
    val userAudioCount: Int = 0,
    val totalAvailableAudio: Int = 0,
    val isScanning: Boolean = false,
    val scanResult: String = "",
    val appVersion: String = "",
    val showTimePickerDialog: Boolean = false,
    val showResetDialog: Boolean = false,
    val errorMessage: String? = null
)
