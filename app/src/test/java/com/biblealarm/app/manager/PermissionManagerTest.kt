package com.biblealarm.app.manager

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * 权限管理器测试
 */
class PermissionManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockAlarmManager: AlarmManager

    private lateinit var permissionManager: PermissionManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockAlarmManager)
        permissionManager = PermissionManager(mockContext)
    }

    @Test
    fun `测试检查通知权限 - Android 13以上`() = runTest {
        // 模拟Android 13环境
        whenever(mockContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        val hasPermission = permissionManager.hasNotificationPermission()

        assertTrue("应该有通知权限", hasPermission)
    }

    @Test
    fun `测试检查通知权限 - Android 13以下`() = runTest {
        // 模拟Android 12环境，通知权限默认授予
        val hasPermission = permissionManager.hasNotificationPermission()

        assertTrue("Android 13以下应该默认有通知权限", hasPermission)
    }

    @Test
    fun `测试检查精确闹钟权限 - 已授权`() = runTest {
        whenever(mockAlarmManager.canScheduleExactAlarms()).thenReturn(true)

        val hasPermission = permissionManager.hasExactAlarmPermission()

        assertTrue("应该有精确闹钟权限", hasPermission)
    }

    @Test
    fun `测试检查精确闹钟权限 - 未授权`() = runTest {
        whenever(mockAlarmManager.canScheduleExactAlarms()).thenReturn(false)

        val hasPermission = permissionManager.hasExactAlarmPermission()

        assertFalse("不应该有精确闹钟权限", hasPermission)
    }

    @Test
    fun `测试检查音频权限 - 已授权`() = runTest {
        whenever(mockContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        val hasPermission = permissionManager.hasAudioPermission()

        assertTrue("应该有音频权限", hasPermission)
    }

    @Test
    fun `测试检查音频权限 - 未授权`() = runTest {
        whenever(mockContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
            .thenReturn(PackageManager.PERMISSION_DENIED)

        val hasPermission = permissionManager.hasAudioPermission()

        assertFalse("不应该有音频权限", hasPermission)
    }

    @Test
    fun `测试检查所有权限 - 全部已授权`() = runTest {
        // 模拟所有权限都已授权
        whenever(mockContext.checkSelfPermission(any())).thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockAlarmManager.canScheduleExactAlarms()).thenReturn(true)

        val hasAllPermissions = permissionManager.hasAllRequiredPermissions()

        assertTrue("应该有所有必需权限", hasAllPermissions)
    }

    @Test
    fun `测试检查所有权限 - 部分未授权`() = runTest {
        // 模拟部分权限未授权
        whenever(mockContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS))
            .thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
            .thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockAlarmManager.canScheduleExactAlarms()).thenReturn(true)

        val hasAllPermissions = permissionManager.hasAllRequiredPermissions()

        assertFalse("不应该有所有必需权限", hasAllPermissions)
    }

    @Test
    fun `测试获取缺失权限列表`() = runTest {
        // 模拟部分权限缺失
        whenever(mockContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS))
            .thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(mockContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
            .thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockContext.checkSelfPermission(Manifest.permission.VIBRATE))
            .thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(mockAlarmManager.canScheduleExactAlarms()).thenReturn(false)

        val missingPermissions = permissionManager.getMissingPermissions()

        assertTrue("应该包含存储权限", missingPermissions.contains("存储权限"))
        assertTrue("应该包含振动权限", missingPermissions.contains("振动权限"))
        assertTrue("应该包含精确闹钟权限", missingPermissions.contains("精确闹钟权限"))
        assertFalse("不应该包含通知权限", missingPermissions.contains("通知权限"))
    }

    @Test
    fun `测试权限状态映射`() = runTest {
        val permissionStatus = permissionManager.getPermissionStatus()

        // 验证权限状态包含所有必需的权限项
        assertTrue("应该包含通知权限状态", permissionStatus.containsKey("通知权限"))
        assertTrue("应该包含精确闹钟权限状态", permissionStatus.containsKey("精确闹钟权限"))
        assertTrue("应该包含存储权限状态", permissionStatus.containsKey("存储权限"))
        assertTrue("应该包含振动权限状态", permissionStatus.containsKey("振动权限"))
    }

    @Test
    fun `测试权限重要性级别`() = runTest {
        val criticalPermissions = permissionManager.getCriticalPermissions()
        val optionalPermissions = permissionManager.getOptionalPermissions()

        // 验证关键权限
        assertTrue("精确闹钟权限应该是关键权限", criticalPermissions.contains("精确闹钟权限"))
        assertTrue("通知权限应该是关键权限", criticalPermissions.contains("通知权限"))

        // 验证可选权限
        assertTrue("振动权限应该是可选权限", optionalPermissions.contains("振动权限"))
        assertTrue("存储权限应该是可选权限", optionalPermissions.contains("存储权限"))
    }

    @Test
    fun `测试Android版本兼容性检查`() = runTest {
        val compatibilityInfo = permissionManager.getAndroidCompatibilityInfo()

        assertNotNull("兼容性信息不应为空", compatibilityInfo)
        assertTrue("应该包含版本信息", compatibilityInfo.containsKey("androidVersion"))
        assertTrue("应该包含API级别", compatibilityInfo.containsKey("apiLevel"))
        assertTrue("应该包含兼容性状态", compatibilityInfo.containsKey("isCompatible"))
    }

    @Test
    fun `测试权限请求建议`() = runTest {
        // 模拟缺失精确闹钟权限
        whenever(mockAlarmManager.canScheduleExactAlarms()).thenReturn(false)
        whenever(mockContext.checkSelfPermission(any())).thenReturn(PackageManager.PERMISSION_GRANTED)

        val suggestions = permissionManager.getPermissionSuggestions()

        assertTrue("应该包含精确闹钟权限建议", 
            suggestions.any { it.contains("精确闹钟") })
    }

    @Test
    fun `测试权限诊断信息`() = runTest {
        val diagnosticInfo = permissionManager.getDiagnosticInfo()

        assertNotNull("诊断信息不应为空", diagnosticInfo)
        assertTrue("应该包含权限状态", diagnosticInfo.containsKey("permissions"))
        assertTrue("应该包含系统信息", diagnosticInfo.containsKey("system"))
        assertTrue("应该包含建议", diagnosticInfo.containsKey("suggestions"))
    }

    @Test
    fun `测试权限变更监听`() = runTest {
        var callbackInvoked = false
        var receivedPermissions: Map<String, Boolean>? = null

        permissionManager.setPermissionChangeListener { permissions ->
            callbackInvoked = true
            receivedPermissions = permissions
        }

        // 模拟权限变更
        permissionManager.notifyPermissionChanged()

        assertTrue("权限变更回调应该被调用", callbackInvoked)
        assertNotNull("应该接收到权限状态", receivedPermissions)
    }

    @Test
    fun `测试批量权限检查性能`() = runTest {
        val startTime = System.currentTimeMillis()

        // 执行多次权限检查
        repeat(100) {
            permissionManager.hasAllRequiredPermissions()
            permissionManager.getMissingPermissions()
            permissionManager.getPermissionStatus()
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // 验证性能（应该在合理时间内完成）
        assertTrue("批量权限检查应该在1秒内完成", duration < 1000)
    }
}