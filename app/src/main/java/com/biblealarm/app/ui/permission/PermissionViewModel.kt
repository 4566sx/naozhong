package com.biblealarm.app.ui.permission

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblealarm.app.manager.PermissionImportance
import com.biblealarm.app.manager.PermissionManager
import com.biblealarm.app.manager.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 权限管理ViewModel
 */
@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()
    
    // 权限状态
    val permissionStates: StateFlow<Map<String, PermissionState>> = permissionManager.permissionStates
    
    // 当前处理的权限
    private var currentPermission: String? = null
    
    init {
        loadInitialData()
    }
    
    /**
     * 加载初始数据
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // 检查所有权限
                permissionManager.checkAllPermissions()
                
                // 获取权限检查结果
                val checkResult = permissionManager.permissionCheckResult.value
                
                // 获取Android版本兼容性
                val compatibility = permissionManager.checkAndroidVersionCompatibility()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    permissionCheckResult = checkResult,
                    androidVersionCompatibility = compatibility
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "加载权限信息失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 检查所有权限
     */
    fun checkAllPermissions() {
        viewModelScope.launch {
            try {
                permissionManager.checkAllPermissions()
                
                val checkResult = permissionManager.permissionCheckResult.value
                _uiState.value = _uiState.value.copy(
                    permissionCheckResult = checkResult
                )
                
            } catch (e: Exception) {
                showError("检查权限失败: ${e.message}")
            }
        }
    }
    
    /**
     * 请求所有缺失权限
     */
    fun requestAllMissingPermissions(
        activity: ComponentActivity,
        multiplePermissionLauncher: ActivityResultLauncher<Array<String>>,
        singlePermissionLauncher: ActivityResultLauncher<String>
    ) {
        viewModelScope.launch {
            try {
                // 请求基础权限
                permissionManager.requestBasicPermissions(activity, multiplePermissionLauncher)
                
                // 请求音频权限
                permissionManager.requestAudioPermissions(activity, multiplePermissionLauncher)
                
                // 请求通知权限
                permissionManager.requestNotificationPermission(activity, singlePermissionLauncher)
                
                // 请求精确闹钟权限
                permissionManager.requestExactAlarmPermission(activity)
                
                showMessage("正在请求权限，请按提示操作")
                
            } catch (e: Exception) {
                showError("请求权限失败: ${e.message}")
            }
        }
    }
    
    /**
     * 请求特定权限
     */
    fun requestSpecificPermission(
        activity: ComponentActivity,
        permission: String,
        multiplePermissionLauncher: ActivityResultLauncher<Array<String>>,
        singlePermissionLauncher: ActivityResultLauncher<String>
    ) {
        viewModelScope.launch {
            try {
                currentPermission = permission
                
                when (permission) {
                    android.Manifest.permission.SCHEDULE_EXACT_ALARM -> {
                        permissionManager.requestExactAlarmPermission(activity)
                    }
                    android.Manifest.permission.POST_NOTIFICATIONS -> {
                        permissionManager.requestNotificationPermission(activity, singlePermissionLauncher)
                    }
                    android.Manifest.permission.READ_MEDIA_AUDIO,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        permissionManager.requestAudioPermissions(activity, multiplePermissionLauncher)
                    }
                    else -> {
                        multiplePermissionLauncher.launch(arrayOf(permission))
                    }
                }
                
                showMessage("正在请求${getPermissionDisplayName(permission)}权限")
                
            } catch (e: Exception) {
                showError("请求权限失败: ${e.message}")
            }
        }
    }
    
    /**
     * 打开权限设置
     */
    fun openPermissionSettings(activity: ComponentActivity, permission: String) {
        viewModelScope.launch {
            try {
                when (permission) {
                    android.Manifest.permission.POST_NOTIFICATIONS -> {
                        permissionManager.openNotificationSettings(activity)
                    }
                    else -> {
                        permissionManager.openAppSettings(activity)
                    }
                }
                
                showMessage("已打开设置页面，请手动开启权限")
                
            } catch (e: Exception) {
                showError("打开设置失败: ${e.message}")
            }
        }
    }
    
    /**
     * 处理权限请求结果
     */
    fun handlePermissionResult(permissions: Map<String, Boolean>) {
        viewModelScope.launch {
            try {
                permissionManager.handlePermissionResult(permissions)
                
                val grantedCount = permissions.values.count { it }
                val totalCount = permissions.size
                
                if (grantedCount == totalCount) {
                    showMessage("所有权限已授予")
                } else {
                    showMessage("已授予 $grantedCount/$totalCount 个权限")
                }
                
                // 重新检查权限状态
                checkAllPermissions()
                
            } catch (e: Exception) {
                showError("处理权限结果失败: ${e.message}")
            }
        }
    }
    
    /**
     * 处理单个权限请求结果
     */
    fun handleSinglePermissionResult(granted: Boolean) {
        viewModelScope.launch {
            try {
                val permission = currentPermission ?: return@launch
                
                permissionManager.handlePermissionResult(mapOf(permission to granted))
                
                if (granted) {
                    showMessage("${getPermissionDisplayName(permission)}权限已授予")
                } else {
                    showMessage("${getPermissionDisplayName(permission)}权限被拒绝")
                }
                
                // 重新检查权限状态
                checkAllPermissions()
                
                currentPermission = null
                
            } catch (e: Exception) {
                showError("处理权限结果失败: ${e.message}")
            }
        }
    }
    
    /**
     * 获取权限描述
     */
    fun getPermissionDescription(permission: String): String {
        return permissionManager.getPermissionDescription(permission)
    }
    
    /**
     * 获取权限重要性
     */
    fun getPermissionImportance(permission: String): PermissionImportance {
        return permissionManager.getPermissionImportance(permission)
    }
    
    /**
     * 获取权限显示名称
     */
    private fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            android.Manifest.permission.SCHEDULE_EXACT_ALARM -> "精确闹钟"
            android.Manifest.permission.USE_EXACT_ALARM -> "使用精确闹钟"
            android.Manifest.permission.POST_NOTIFICATIONS -> "通知"
            android.Manifest.permission.READ_MEDIA_AUDIO -> "音频文件访问"
            android.Manifest.permission.READ_EXTERNAL_STORAGE -> "存储访问"
            android.Manifest.permission.WAKE_LOCK -> "保持唤醒"
            android.Manifest.permission.VIBRATE -> "振动"
            android.Manifest.permission.RECEIVE_BOOT_COMPLETED -> "开机启动"
            android.Manifest.permission.FOREGROUND_SERVICE -> "前台服务"
            android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK -> "媒体播放服务"
            else -> permission.substringAfterLast(".")
        }
    }
    
    /**
     * 检查是否有所有必需权限
     */
    fun hasAllRequiredPermissions(): Boolean {
        return permissionManager.hasAllRequiredPermissions()
    }
    
    /**
     * 获取缺失权限列表
     */
    fun getMissingPermissions(): List<String> {
        return permissionManager.getMissingPermissions()
    }
    
    /**
     * 获取权限状态摘要
     */
    fun getPermissionSummary(): String {
        return permissionManager.getPermissionSummary()
    }
    
    /**
     * 刷新权限状态
     */
    fun refreshPermissions() {
        checkAllPermissions()
    }
    
    /**
     * 重置权限状态
     */
    fun resetPermissions() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    errorMessage = null,
                    message = null
                )
                
                checkAllPermissions()
                
            } catch (e: Exception) {
                showError("重置权限状态失败: ${e.message}")
            }
        }
    }
    
    /**
     * 获取权限帮助信息
     */
    fun getPermissionHelpInfo(): String {
        return buildString {
            append("权限说明：\n\n")
            append("🔴 关键权限：应用正常运行必需的权限\n")
            append("🟠 重要权限：影响主要功能的权限\n")
            append("🔵 一般权限：增强用户体验的权限\n")
            append("⚪ 可选权限：额外功能的权限\n\n")
            append("如果权限被拒绝，您可以：\n")
            append("1. 点击"请求权限"按钮重新申请\n")
            append("2. 点击设置按钮手动开启\n")
            append("3. 在系统设置中找到本应用进行设置")
        }
    }
    
    /**
     * 检查权限兼容性
     */
    fun checkPermissionCompatibility(): String {
        val compatibility = permissionManager.checkAndroidVersionCompatibility()
        
        return buildString {
            append("Android版本兼容性：\n")
            append("当前版本：API ${compatibility.currentSdkVersion}\n")
            append("目标版本：API ${compatibility.targetSdkVersion}\n")
            append("兼容状态：${if (compatibility.isFullyCompatible) "完全兼容" else "部分兼容"}\n\n")
            
            if (compatibility.supportedFeatures.isNotEmpty()) {
                append("支持的功能：\n")
                compatibility.supportedFeatures.forEach { feature ->
                    append("✓ $feature\n")
                }
                append("\n")
            }
            
            if (compatibility.limitedFeatures.isNotEmpty()) {
                append("受限功能：\n")
                compatibility.limitedFeatures.forEach { limitation ->
                    append("⚠ $limitation\n")
                }
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
     * 获取权限统计信息
     */
    fun getPermissionStats(): PermissionStats {
        val checkResult = _uiState.value.permissionCheckResult
        val states = permissionStates.value
        
        return PermissionStats(
            totalPermissions = checkResult.totalPermissions,
            grantedPermissions = checkResult.grantedPermissions,
            deniedPermissions = checkResult.deniedPermissions,
            criticalMissing = checkResult.criticalMissing.size,
            hasAllRequired = checkResult.hasAllRequired,
            completionPercentage = if (checkResult.totalPermissions > 0) {
                (checkResult.grantedPermissions.toFloat() / checkResult.totalPermissions.toFloat() * 100).toInt()
            } else 0
        )
    }
}

/**
 * 权限UI状态
 */
data class PermissionUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null,
    val permissionCheckResult: com.biblealarm.app.manager.PermissionCheckResult = com.biblealarm.app.manager.PermissionCheckResult(),
    val androidVersionCompatibility: com.biblealarm.app.manager.AndroidVersionCompatibility? = null
)

/**
 * 权限统计数据类
 */
data class PermissionStats(
    val totalPermissions: Int,
    val grantedPermissions: Int,
    val deniedPermissions: Int,
    val criticalMissing: Int,
    val hasAllRequired: Boolean,
    val completionPercentage: Int
)