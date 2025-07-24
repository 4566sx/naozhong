package com.biblealarm.app.manager

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android 15权限管理器
 * 处理应用所需的各种权限请求和管理
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "PermissionManager"
        
        // 权限请求码
        const val REQUEST_CODE_EXACT_ALARM = 1001
        const val REQUEST_CODE_NOTIFICATION = 1002
        const val REQUEST_CODE_AUDIO = 1003
        const val REQUEST_CODE_STORAGE = 1004
        const val REQUEST_CODE_PHONE_STATE = 1005
        const val REQUEST_CODE_WAKE_LOCK = 1006
        
        // Android 15 新增权限
        val ANDROID_15_PERMISSIONS = arrayOf(
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            Manifest.permission.USE_EXACT_ALARM,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.VIBRATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
        )
        
        // 基础权限
        val BASIC_PERMISSIONS = arrayOf(
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.VIBRATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        )
        
        // 音频权限
        val AUDIO_PERMISSIONS = arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        
        // 通知权限
        val NOTIFICATION_PERMISSIONS = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
    
    // 权限状态
    private val _permissionStates = MutableStateFlow<Map<String, PermissionState>>(emptyMap())
    val permissionStates: StateFlow<Map<String, PermissionState>> = _permissionStates.asStateFlow()
    
    // 权限检查结果
    private val _permissionCheckResult = MutableStateFlow(PermissionCheckResult())
    val permissionCheckResult: StateFlow<PermissionCheckResult> = _permissionCheckResult.asStateFlow()
    
    init {
        checkAllPermissions()
    }
    
    /**
     * 检查所有权限状态
     */
    fun checkAllPermissions() {
        val permissionMap = mutableMapOf<String, PermissionState>()
        
        // 检查基础权限
        BASIC_PERMISSIONS.forEach { permission ->
            permissionMap[permission] = checkPermissionState(permission)
        }
        
        // 检查音频权限
        AUDIO_PERMISSIONS.forEach { permission ->
            permissionMap[permission] = checkPermissionState(permission)
        }
        
        // 检查通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NOTIFICATION_PERMISSIONS.forEach { permission ->
                permissionMap[permission] = checkPermissionState(permission)
            }
        }
        
        // 检查精确闹钟权限
        permissionMap[Manifest.permission.SCHEDULE_EXACT_ALARM] = checkExactAlarmPermission()
        
        // 检查前台服务权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionMap[Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK] = 
                checkPermissionState(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
        }
        
        _permissionStates.value = permissionMap
        
        // 更新权限检查结果
        updatePermissionCheckResult(permissionMap)
        
        Log.d(TAG, "权限检查完成: ${permissionMap.size}个权限")
    }
    
    /**
     * 检查单个权限状态
     */
    private fun checkPermissionState(permission: String): PermissionState {
        return when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                PermissionState.GRANTED
            }
            else -> {
                PermissionState.DENIED
            }
        }
    }
    
    /**
     * 检查精确闹钟权限
     */
    private fun checkExactAlarmPermission(): PermissionState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                PermissionState.GRANTED
            } else {
                PermissionState.DENIED
            }
        } else {
            PermissionState.GRANTED // Android 12以下默认有权限
        }
    }
    
    /**
     * 检查通知权限
     */
    fun checkNotificationPermission(): PermissionState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // 检查通知渠道是否启用
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.areNotificationsEnabled()) {
                PermissionState.GRANTED
            } else {
                PermissionState.DENIED
            }
        }
    }
    
    /**
     * 请求基础权限
     */
    fun requestBasicPermissions(activity: ComponentActivity, launcher: ActivityResultLauncher<Array<String>>) {
        val deniedPermissions = BASIC_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (deniedPermissions.isNotEmpty()) {
            launcher.launch(deniedPermissions)
            Log.d(TAG, "请求基础权限: ${deniedPermissions.joinToString()}")
        }
    }
    
    /**
     * 请求音频权限
     */
    fun requestAudioPermissions(activity: ComponentActivity, launcher: ActivityResultLauncher<Array<String>>) {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        val deniedPermissions = permissionsToRequest.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (deniedPermissions.isNotEmpty()) {
            launcher.launch(deniedPermissions)
            Log.d(TAG, "请求音频权限: ${deniedPermissions.joinToString()}")
        }
    }
    
    /**
     * 请求通知权限
     */
    fun requestNotificationPermission(activity: ComponentActivity, launcher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                Log.d(TAG, "请求通知权限")
            }
        }
    }
    
    /**
     * 请求精确闹钟权限
     */
    fun requestExactAlarmPermission(activity: ComponentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    activity.startActivity(intent)
                    Log.d(TAG, "请求精确闹钟权限")
                } catch (e: Exception) {
                    Log.e(TAG, "请求精确闹钟权限失败", e)
                }
            }
        }
    }
    
    /**
     * 打开应用设置页面
     */
    fun openAppSettings(activity: ComponentActivity) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            activity.startActivity(intent)
            Log.d(TAG, "打开应用设置页面")
        } catch (e: Exception) {
            Log.e(TAG, "打开应用设置页面失败", e)
        }
    }
    
    /**
     * 打开通知设置页面
     */
    fun openNotificationSettings(activity: ComponentActivity) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
            activity.startActivity(intent)
            Log.d(TAG, "打开通知设置页面")
        } catch (e: Exception) {
            Log.e(TAG, "打开通知设置页面失败", e)
        }
    }
    
    /**
     * 检查是否有所有必需权限
     */
    fun hasAllRequiredPermissions(): Boolean {
        val currentStates = _permissionStates.value
        
        // 检查基础权限
        val hasBasicPermissions = BASIC_PERMISSIONS.all { permission ->
            currentStates[permission] == PermissionState.GRANTED
        }
        
        // 检查精确闹钟权限
        val hasExactAlarmPermission = currentStates[Manifest.permission.SCHEDULE_EXACT_ALARM] == PermissionState.GRANTED
        
        // 检查通知权限
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentStates[Manifest.permission.POST_NOTIFICATIONS] == PermissionState.GRANTED
        } else {
            checkNotificationPermission() == PermissionState.GRANTED
        }
        
        return hasBasicPermissions && hasExactAlarmPermission && hasNotificationPermission
    }
    
    /**
     * 获取缺失的权限列表
     */
    fun getMissingPermissions(): List<String> {
        val currentStates = _permissionStates.value
        return currentStates.filter { (_, state) -> 
            state != PermissionState.GRANTED 
        }.keys.toList()
    }
    
    /**
     * 获取权限描述
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.SCHEDULE_EXACT_ALARM -> "精确闹钟权限 - 用于准时触发闹钟"
            Manifest.permission.USE_EXACT_ALARM -> "使用精确闹钟权限"
            Manifest.permission.POST_NOTIFICATIONS -> "通知权限 - 用于显示闹钟通知"
            Manifest.permission.READ_MEDIA_AUDIO -> "音频文件访问权限 - 用于读取诗篇音频文件"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "存储权限 - 用于读取音频文件"
            Manifest.permission.WAKE_LOCK -> "唤醒锁权限 - 用于保持设备唤醒"
            Manifest.permission.VIBRATE -> "振动权限 - 用于闹钟振动提醒"
            Manifest.permission.RECEIVE_BOOT_COMPLETED -> "开机启动权限 - 用于重新设置闹钟"
            Manifest.permission.FOREGROUND_SERVICE -> "前台服务权限 - 用于播放音频"
            Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK -> "媒体播放前台服务权限"
            else -> "未知权限"
        }
    }
    
    /**
     * 获取权限重要性级别
     */
    fun getPermissionImportance(permission: String): PermissionImportance {
        return when (permission) {
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            Manifest.permission.USE_EXACT_ALARM -> PermissionImportance.CRITICAL
            
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.WAKE_LOCK -> PermissionImportance.HIGH
            
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE -> PermissionImportance.MEDIUM
            
            else -> PermissionImportance.LOW
        }
    }
    
    /**
     * 更新权限检查结果
     */
    private fun updatePermissionCheckResult(permissionMap: Map<String, PermissionState>) {
        val grantedCount = permissionMap.values.count { it == PermissionState.GRANTED }
        val deniedCount = permissionMap.values.count { it == PermissionState.DENIED }
        val totalCount = permissionMap.size
        
        val criticalMissing = permissionMap.filter { (permission, state) ->
            state != PermissionState.GRANTED && getPermissionImportance(permission) == PermissionImportance.CRITICAL
        }.keys.toList()
        
        val result = PermissionCheckResult(
            totalPermissions = totalCount,
            grantedPermissions = grantedCount,
            deniedPermissions = deniedCount,
            hasAllRequired = hasAllRequiredPermissions(),
            criticalMissing = criticalMissing,
            missingPermissions = getMissingPermissions()
        )
        
        _permissionCheckResult.value = result
    }
    
    /**
     * 处理权限请求结果
     */
    fun handlePermissionResult(permissions: Map<String, Boolean>) {
        permissions.forEach { (permission, granted) ->
            val currentStates = _permissionStates.value.toMutableMap()
            currentStates[permission] = if (granted) PermissionState.GRANTED else PermissionState.DENIED
            _permissionStates.value = currentStates
            
            Log.d(TAG, "权限结果: $permission = ${if (granted) "已授予" else "被拒绝"}")
        }
        
        // 重新检查所有权限
        checkAllPermissions()
    }
    
    /**
     * 获取权限状态摘要
     */
    fun getPermissionSummary(): String {
        val result = _permissionCheckResult.value
        return buildString {
            append("权限状态摘要:\n")
            append("总计: ${result.totalPermissions}个权限\n")
            append("已授予: ${result.grantedPermissions}个\n")
            append("被拒绝: ${result.deniedPermissions}个\n")
            append("状态: ${if (result.hasAllRequired) "完整" else "不完整"}\n")
            
            if (result.criticalMissing.isNotEmpty()) {
                append("关键缺失: ${result.criticalMissing.joinToString()}")
            }
        }
    }
    
    /**
     * 检查Android版本兼容性
     */
    fun checkAndroidVersionCompatibility(): AndroidVersionCompatibility {
        val currentSdk = Build.VERSION.SDK_INT
        
        return AndroidVersionCompatibility(
            currentSdkVersion = currentSdk,
            targetSdkVersion = 35, // Android 15
            isFullyCompatible = currentSdk >= Build.VERSION_CODES.VANILLA_ICE_CREAM,
            supportedFeatures = getSupportedFeatures(currentSdk),
            limitedFeatures = getLimitedFeatures(currentSdk)
        )
    }
    
    /**
     * 获取支持的功能列表
     */
    private fun getSupportedFeatures(sdkVersion: Int): List<String> {
        val features = mutableListOf<String>()
        
        features.add("基础闹钟功能")
        features.add("音频播放")
        features.add("本地存储")
        
        if (sdkVersion >= Build.VERSION_CODES.M) {
            features.add("运行时权限")
        }
        
        if (sdkVersion >= Build.VERSION_CODES.O) {
            features.add("通知渠道")
            features.add("后台限制")
        }
        
        if (sdkVersion >= Build.VERSION_CODES.S) {
            features.add("精确闹钟权限")
        }
        
        if (sdkVersion >= Build.VERSION_CODES.TIRAMISU) {
            features.add("通知权限")
            features.add("媒体权限")
        }
        
        if (sdkVersion >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            features.add("前台服务类型")
        }
        
        return features
    }
    
    /**
     * 获取受限功能列表
     */
    private fun getLimitedFeatures(sdkVersion: Int): List<String> {
        val limitations = mutableListOf<String>()
        
        if (sdkVersion < Build.VERSION_CODES.S) {
            limitations.add("无精确闹钟权限管理")
        }
        
        if (sdkVersion < Build.VERSION_CODES.TIRAMISU) {
            limitations.add("无细粒度媒体权限")
        }
        
        if (sdkVersion < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            limitations.add("无前台服务类型限制")
        }
        
        return limitations
    }
}

/**
 * 权限状态枚举
 */
enum class PermissionState {
    GRANTED,    // 已授予
    DENIED,     // 被拒绝
    UNKNOWN     // 未知状态
}

/**
 * 权限重要性级别
 */
enum class PermissionImportance {
    CRITICAL,   // 关键权限
    HIGH,       // 高重要性
    MEDIUM,     // 中等重要性
    LOW         // 低重要性
}

/**
 * 权限检查结果数据类
 */
data class PermissionCheckResult(
    val totalPermissions: Int = 0,
    val grantedPermissions: Int = 0,
    val deniedPermissions: Int = 0,
    val hasAllRequired: Boolean = false,
    val criticalMissing: List<String> = emptyList(),
    val missingPermissions: List<String> = emptyList()
)

/**
 * Android版本兼容性数据类
 */
data class AndroidVersionCompatibility(
    val currentSdkVersion: Int,
    val targetSdkVersion: Int,
    val isFullyCompatible: Boolean,
    val supportedFeatures: List<String>,
    val limitedFeatures: List<String>
)