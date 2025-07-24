package com.biblealarm.app.ui.permission

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biblealarm.app.manager.PermissionImportance
import com.biblealarm.app.manager.PermissionState
import com.biblealarm.app.ui.theme.BibleAlarmTheme
import com.biblealarm.app.ui.theme.DarkBrown
import com.biblealarm.app.ui.theme.GoldAccent
import com.biblealarm.app.ui.theme.WarmWhite

/**
 * 权限管理界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    onNavigateBack: () -> Unit,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionStates by viewModel.permissionStates.collectAsStateWithLifecycle()
    val context = LocalContext.current as ComponentActivity
    
    // 权限请求启动器
    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.handlePermissionResult(permissions)
    }
    
    val singlePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.handleSinglePermissionResult(granted)
    }
    
    LaunchedEffect(Unit) {
        viewModel.checkAllPermissions()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部应用栏
        TopAppBar(
            title = { 
                Text(
                    text = "权限管理",
                    color = DarkBrown,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = DarkBrown
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = WarmWhite
            )
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 权限状态概览
            item {
                PermissionOverviewCard(
                    checkResult = uiState.permissionCheckResult,
                    onRequestAllPermissions = {
                        viewModel.requestAllMissingPermissions(
                            context,
                            multiplePermissionLauncher,
                            singlePermissionLauncher
                        )
                    }
                )
            }
            
            // 权限列表
            items(permissionStates.toList()) { (permission, state) ->
                PermissionItemCard(
                    permission = permission,
                    state = state,
                    description = viewModel.getPermissionDescription(permission),
                    importance = viewModel.getPermissionImportance(permission),
                    onRequestPermission = {
                        viewModel.requestSpecificPermission(
                            context,
                            permission,
                            multiplePermissionLauncher,
                            singlePermissionLauncher
                        )
                    },
                    onOpenSettings = {
                        viewModel.openPermissionSettings(context, permission)
                    }
                )
            }
            
            // Android版本兼容性信息
            item {
                AndroidVersionCard(
                    compatibility = uiState.androidVersionCompatibility
                )
            }
            
            // 帮助信息
            item {
                HelpCard()
            }
        }
    }
}

/**
 * 权限概览卡片
 */
@Composable
private fun PermissionOverviewCard(
    checkResult: com.biblealarm.app.manager.PermissionCheckResult,
    onRequestAllPermissions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "权限状态",
                    style = MaterialTheme.typography.titleLarge,
                    color = DarkBrown,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = if (checkResult.hasAllRequired) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = if (checkResult.hasAllRequired) "完整" else "不完整",
                    tint = if (checkResult.hasAllRequired) Color.Green else Color.Orange,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 进度条
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = if (checkResult.totalPermissions > 0) {
                        checkResult.grantedPermissions.toFloat() / checkResult.totalPermissions.toFloat()
                    } else 0f,
                    modifier = Modifier.fillMaxWidth(),
                    color = GoldAccent,
                    trackColor = GoldAccent.copy(alpha = 0.3f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "已授予: ${checkResult.grantedPermissions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green
                    )
                    Text(
                        text = "总计: ${checkResult.totalPermissions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkBrown.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 状态描述
            Text(
                text = if (checkResult.hasAllRequired) {
                    "所有必需权限已授予，应用可以正常运行。"
                } else {
                    "缺少 ${checkResult.deniedPermissions} 个权限，可能影响应用功能。"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = DarkBrown.copy(alpha = 0.8f)
            )
            
            // 关键权限缺失警告
            if (checkResult.criticalMissing.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⚠️ 关键权限缺失",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "以下权限对应用正常运行至关重要：",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        checkResult.criticalMissing.forEach { permission ->
                            Text(
                                text = "• ${getPermissionDisplayName(permission)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // 一键授权按钮
            if (!checkResult.hasAllRequired) {
                Button(
                    onClick = onRequestAllPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "一键授权所有权限",
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 权限项卡片
 */
@Composable
private fun PermissionItemCard(
    permission: String,
    state: PermissionState,
    description: String,
    importance: PermissionImportance,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 权限标题和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = getPermissionDisplayName(permission),
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkBrown,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // 重要性标签
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val (importanceText, importanceColor) = when (importance) {
                            PermissionImportance.CRITICAL -> "关键" to Color.Red
                            PermissionImportance.HIGH -> "重要" to Color.Orange
                            PermissionImportance.MEDIUM -> "一般" to Color.Blue
                            PermissionImportance.LOW -> "可选" to Color.Gray
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = importanceColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = importanceText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = importanceColor
                            )
                        }
                    }
                }
                
                // 状态图标
                val (icon, iconColor) = when (state) {
                    PermissionState.GRANTED -> Icons.Default.CheckCircle to Color.Green
                    PermissionState.DENIED -> Icons.Default.Cancel to Color.Red
                    PermissionState.UNKNOWN -> Icons.Default.Help to Color.Gray
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = state.name,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 权限描述
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = DarkBrown.copy(alpha = 0.7f)
            )
            
            // 操作按钮
            if (state != PermissionState.GRANTED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRequestPermission,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GoldAccent
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(GoldAccent)
                        )
                    ) {
                        Text(text = "请求权限")
                    }
                    
                    OutlinedButton(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DarkBrown
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Android版本兼容性卡片
 */
@Composable
private fun AndroidVersionCard(
    compatibility: com.biblealarm.app.manager.AndroidVersionCompatibility?
) {
    if (compatibility == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Android版本",
                    tint = GoldAccent
                )
                Text(
                    text = "Android版本兼容性",
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkBrown,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "当前版本: API ${compatibility.currentSdkVersion}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkBrown
                )
                Text(
                    text = "目标版本: API ${compatibility.targetSdkVersion}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkBrown
                )
            }
            
            // 兼容性状态
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (compatibility.isFullyCompatible) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = if (compatibility.isFullyCompatible) "完全兼容" else "部分兼容",
                    tint = if (compatibility.isFullyCompatible) Color.Green else Color.Orange,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (compatibility.isFullyCompatible) "完全兼容" else "部分兼容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (compatibility.isFullyCompatible) Color.Green else Color.Orange
                )
            }
            
            // 支持的功能
            if (compatibility.supportedFeatures.isNotEmpty()) {
                Text(
                    text = "支持的功能:",
                    style = MaterialTheme.typography.titleSmall,
                    color = DarkBrown,
                    fontWeight = FontWeight.Medium
                )
                compatibility.supportedFeatures.forEach { feature ->
                    Text(
                        text = "✓ $feature",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green
                    )
                }
            }
            
            // 受限功能
            if (compatibility.limitedFeatures.isNotEmpty()) {
                Text(
                    text = "受限功能:",
                    style = MaterialTheme.typography.titleSmall,
                    color = DarkBrown,
                    fontWeight = FontWeight.Medium
                )
                compatibility.limitedFeatures.forEach { limitation ->
                    Text(
                        text = "⚠ $limitation",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Orange
                    )
                }
            }
        }
    }
}

/**
 * 帮助信息卡片
 */
@Composable
private fun HelpCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Help,
                    contentDescription = "帮助",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "权限说明",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "• 关键权限：应用正常运行必需的权限\n" +
                      "• 重要权限：影响主要功能的权限\n" +
                      "• 一般权限：增强用户体验的权限\n" +
                      "• 可选权限：额外功能的权限",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "如果权限被拒绝，您可以在系统设置中手动开启。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 获取权限显示名称
 */
private fun getPermissionDisplayName(permission: String): String {
    return when (permission) {
        android.Manifest.permission.SCHEDULE_EXACT_ALARM -> "精确闹钟"
        android.Manifest.permission.USE_EXACT_ALARM -> "使用精确闹钟"
        android.Manifest.permission.POST_NOTIFICATIONS -> "通知权限"
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