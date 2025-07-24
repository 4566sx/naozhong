package com.biblealarm.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biblealarm.app.ui.theme.DarkBrown
import com.biblealarm.app.ui.theme.GoldAccent
import com.biblealarm.app.ui.theme.WarmWhite

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAudioSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
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
                    text = "设置",
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
            // 音频设置
            item {
                SettingsSection(
                    title = "音频设置",
                    icon = Icons.Default.VolumeUp
                ) {
                    SettingsItem(
                        title = "音频资源路径",
                        subtitle = uiState.audioPath.ifEmpty { "未设置" },
                        icon = Icons.Default.Folder,
                        onClick = onNavigateToAudioSettings
                    )
                    
                    SettingsItem(
                        title = "默认音量",
                        subtitle = "${(uiState.defaultVolume * 100).toInt()}%",
                        icon = Icons.Default.VolumeUp,
                        trailing = {
                            Slider(
                                value = uiState.defaultVolume,
                                onValueChange = { viewModel.updateDefaultVolume(it) },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldAccent,
                                    activeTrackColor = GoldAccent,
                                    inactiveTrackColor = GoldAccent.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    )
                    
                    SettingsItem(
                        title = "渐强播放",
                        subtitle = if (uiState.fadeInEnabled) "启用" else "禁用",
                        icon = Icons.Default.TrendingUp,
                        trailing = {
                            Switch(
                                checked = uiState.fadeInEnabled,
                                onCheckedChange = { viewModel.updateFadeInEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GoldAccent,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = DarkBrown.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                }
            }
            
            // 闹钟设置
            item {
                SettingsSection(
                    title = "闹钟设置",
                    icon = Icons.Default.Alarm
                ) {
                    SettingsItem(
                        title = "贪睡时长",
                        subtitle = "${uiState.snoozeDuration} 分钟",
                        icon = Icons.Default.Snooze,
                        trailing = {
                            DropdownMenuBox(
                                options = listOf(5, 10, 15, 20, 30),
                                selectedOption = uiState.snoozeDuration,
                                onOptionSelected = { viewModel.updateSnoozeDuration(it) }
                            )
                        }
                    )
                    
                    SettingsItem(
                        title = "振动提醒",
                        subtitle = if (uiState.vibrateEnabled) "启用" else "禁用",
                        icon = Icons.Default.Vibration,
                        trailing = {
                            Switch(
                                checked = uiState.vibrateEnabled,
                                onCheckedChange = { viewModel.updateVibrateEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GoldAccent,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = DarkBrown.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                    
                    SettingsItem(
                        title = "闹钟超时时间",
                        subtitle = "${uiState.alarmTimeout} 分钟后自动停止",
                        icon = Icons.Default.Timer,
                        trailing = {
                            DropdownMenuBox(
                                options = listOf(1, 2, 5, 10, 15),
                                selectedOption = uiState.alarmTimeout,
                                onOptionSelected = { viewModel.updateAlarmTimeout(it) }
                            )
                        }
                    )
                }
            }
            
            // 诗篇设置
            item {
                SettingsSection(
                    title = "诗篇设置",
                    icon = Icons.Default.MenuBook
                ) {
                    SettingsItem(
                        title = "每日自动选择",
                        subtitle = if (uiState.dailyPsalmEnabled) "启用" else "禁用",
                        icon = Icons.Default.AutoMode,
                        trailing = {
                            Switch(
                                checked = uiState.dailyPsalmEnabled,
                                onCheckedChange = { viewModel.updateDailyPsalmEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GoldAccent,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = DarkBrown.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                    
                    SettingsItem(
                        title = "选择时间",
                        subtitle = "${String.format("%02d:%02d", uiState.psalmSelectionHour, uiState.psalmSelectionMinute)}",
                        icon = Icons.Default.Schedule,
                        enabled = uiState.dailyPsalmEnabled,
                        onClick = { viewModel.showTimePickerDialog() }
                    )
                    
                    SettingsItem(
                        title = "随机种子重置",
                        subtitle = "每天使用不同的随机种子",
                        icon = Icons.Default.Shuffle,
                        trailing = {
                            Switch(
                                checked = uiState.randomSeedReset,
                                onCheckedChange = { viewModel.updateRandomSeedReset(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GoldAccent,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = DarkBrown.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                }
            }
            
            // 系统设置
            item {
                SettingsSection(
                    title = "系统设置",
                    icon = Icons.Default.Settings
                ) {
                    SettingsItem(
                        title = "权限管理",
                        subtitle = "管理应用权限",
                        icon = Icons.Default.Security,
                        onClick = onNavigateToPermissions
                    )
                    
                    SettingsItem(
                        title = "开机自启",
                        subtitle = if (uiState.autoStartEnabled) "启用" else "禁用",
                        icon = Icons.Default.PowerSettingsNew,
                        trailing = {
                            Switch(
                                checked = uiState.autoStartEnabled,
                                onCheckedChange = { viewModel.updateAutoStartEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GoldAccent,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = DarkBrown.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                    
                    SettingsItem(
                        title = "后台运行优化",
                        subtitle = "优化后台运行性能",
                        icon = Icons.Default.BatteryAlert,
                        onClick = { viewModel.openBatteryOptimizationSettings() }
                    )
                }
            }
            
            // 关于
            item {
                SettingsSection(
                    title = "关于",
                    icon = Icons.Default.Info
                ) {
                    SettingsItem(
                        title = "应用版本",
                        subtitle = uiState.appVersion,
                        icon = Icons.Default.AppSettingsAlt
                    )
                    
                    SettingsItem(
                        title = "检查更新",
                        subtitle = "查看是否有新版本",
                        icon = Icons.Default.SystemUpdate,
                        onClick = { viewModel.checkForUpdates() }
                    )
                    
                    SettingsItem(
                        title = "用户反馈",
                        subtitle = "发送反馈和建议",
                        icon = Icons.Default.Feedback,
                        onClick = { viewModel.openFeedback() }
                    )
                    
                    SettingsItem(
                        title = "隐私政策",
                        subtitle = "查看隐私政策",
                        icon = Icons.Default.PrivacyTip,
                        onClick = { viewModel.openPrivacyPolicy() }
                    )
                }
            }
            
            // 数据管理
            item {
                SettingsSection(
                    title = "数据管理",
                    icon = Icons.Default.Storage
                ) {
                    SettingsItem(
                        title = "清除缓存",
                        subtitle = "清除应用缓存数据",
                        icon = Icons.Default.CleaningServices,
                        onClick = { viewModel.clearCache() }
                    )
                    
                    SettingsItem(
                        title = "导出设置",
                        subtitle = "备份应用设置",
                        icon = Icons.Default.FileUpload,
                        onClick = { viewModel.exportSettings() }
                    )
                    
                    SettingsItem(
                        title = "导入设置",
                        subtitle = "恢复应用设置",
                        icon = Icons.Default.FileDownload,
                        onClick = { viewModel.importSettings() }
                    )
                    
                    SettingsItem(
                        title = "重置应用",
                        subtitle = "恢复所有默认设置",
                        icon = Icons.Default.RestartAlt,
                        onClick = { viewModel.showResetDialog() },
                        textColor = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // 底部间距
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // 时间选择器对话框
    if (uiState.showTimePickerDialog) {
        TimePickerDialog(
            initialHour = uiState.psalmSelectionHour,
            initialMinute = uiState.psalmSelectionMinute,
            onTimeSelected = { hour, minute ->
                viewModel.updatePsalmSelectionTime(hour, minute)
            },
            onDismiss = { viewModel.hideTimePickerDialog() }
        )
    }
    
    // 重置确认对话框
    if (uiState.showResetDialog) {
        ResetConfirmDialog(
            onConfirm = { viewModel.resetApp() },
            onDismiss = { viewModel.hideResetDialog() }
        )
    }
    
    // 错误提示
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // 显示错误提示
        }
    }
}

/**
 * 设置分组组件
 */
@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 分组标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // 分组内容
            content()
        }
    }
}

/**
 * 设置项组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    textColor: Color = DarkBrown,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(enabled = enabled) { onClick() }
    } else {
        Modifier
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 图标
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (enabled) GoldAccent else GoldAccent.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        
        // 文本内容
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
            
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) textColor.copy(alpha = 0.7f) else textColor.copy(alpha = 0.4f)
                )
            }
        }
        
        // 尾部内容
        trailing?.invoke()
    }
}

/**
 * 下拉菜单选择框
 */
@Composable
private fun DropdownMenuBox(
    options: List<Int>,
    selectedOption: Int,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = GoldAccent
            )
        ) {
            Text(text = selectedOption.toString())
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "选择",
                modifier = Modifier.size(16.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.toString()) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 时间选择器对话框
 */
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择时间",
                color = DarkBrown,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 小时选择
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "小时",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkBrown.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format("%02d", selectedHour),
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DarkBrown
                )
                
                // 分钟选择
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "分钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkBrown.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format("%02d", selectedMinute),
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(selectedHour, selectedMinute)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent
                )
            ) {
                Text(
                    text = "确定",
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = DarkBrown
                )
            }
        },
        containerColor = WarmWhite
    )
}

/**
 * 重置确认对话框
 */
@Composable
private fun ResetConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "警告",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "重置应用",
                color = DarkBrown,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "此操作将清除所有设置和数据，包括：\n• 所有闹钟设置\n• 音频配置\n• 个人偏好设置\n\n此操作不可撤销，确定要继续吗？",
                color = DarkBrown
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "重置",
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = DarkBrown
                )
            }
        },
        containerColor = WarmWhite
    )
}