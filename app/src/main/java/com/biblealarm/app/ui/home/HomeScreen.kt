package com.biblealarm.app.ui.home

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biblealarm.app.ui.components.PsalmCard
import com.biblealarm.app.ui.components.VolumeControl
import com.biblealarm.app.ui.components.NextAlarmCard
import com.biblealarm.app.ui.theme.DarkBrown
import com.biblealarm.app.ui.theme.GoldAccent
import com.biblealarm.app.ui.theme.WarmWhite

/**
 * 主屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAlarms: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadTodaysPsalm()
        viewModel.loadNextAlarm()
        viewModel.checkPermissions()
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
                    text = "圣经闹钟",
                    color = DarkBrown,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                // 权限状态指示器
                if (!uiState.hasAllPermissions) {
                    IconButton(onClick = onNavigateToPermissions) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "权限设置",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 权限提醒卡片
            if (!uiState.hasAllPermissions) {
                item {
                    PermissionReminderCard(
                        onNavigateToPermissions = onNavigateToPermissions
                    )
                }
            }
            
            // 当日诗篇卡片
            item {
                PsalmCard(
                    psalm = uiState.todaysPsalm,
                    isPlaying = uiState.isPlaying,
                    onPlayClick = { viewModel.playPsalm() },
                    onPauseClick = { viewModel.pausePsalm() },
                    onNextClick = { viewModel.nextPsalm() },
                    onPreviousClick = { viewModel.previousPsalm() }
                )
            }
            
            // 音量控制
            item {
                VolumeControl(
                    volume = uiState.volume,
                    isMuted = uiState.isMuted,
                    onVolumeChange = { viewModel.setVolume(it) },
                    onMuteToggle = { viewModel.toggleMute() }
                )
            }
            
            // 下一个闹钟
            item {
                NextAlarmCard(
                    alarm = uiState.nextAlarm,
                    timeUntilAlarm = uiState.timeUntilNextAlarm
                )
            }
            
            // 快捷操作
            item {
                QuickActionsCard(
                    onAddAlarm = onNavigateToAlarms,
                    onViewAlarms = onNavigateToAlarms,
                    onSettings = onNavigateToSettings,
                    onPermissions = onNavigateToPermissions
                )
            }
            
            // 应用状态信息
            item {
                AppStatusCard(
                    isAudioServiceRunning = uiState.isAudioServiceRunning,
                    totalAlarms = uiState.totalAlarms,
                    enabledAlarms = uiState.enabledAlarms
                )
            }
        }
    }
    
    // 错误提示
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // 可以在这里显示Snackbar或其他错误提示
        }
    }
}

/**
 * 权限提醒卡片
 */
@Composable
private fun PermissionReminderCard(
    onNavigateToPermissions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "警告",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "权限不完整",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "应用需要额外权限才能正常工作",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            TextButton(
                onClick = onNavigateToPermissions,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(text = "设置")
            }
        }
    }
}

/**
 * 快捷操作卡片
 */
@Composable
private fun QuickActionsCard(
    onAddAlarm: () -> Unit,
    onViewAlarms: () -> Unit,
    onSettings: () -> Unit,
    onPermissions: () -> Unit
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
            Text(
                text = "快捷操作",
                style = MaterialTheme.typography.titleMedium,
                color = DarkBrown,
                fontWeight = FontWeight.SemiBold
            )
            
            // 第一行按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    text = "添加闹钟",
                    onClick = onAddAlarm
                )
                
                QuickActionButton(
                    icon = Icons.Default.List,
                    text = "闹钟列表",
                    onClick = onViewAlarms
                )
                
                QuickActionButton(
                    icon = Icons.Default.Settings,
                    text = "设置",
                    onClick = onSettings
                )
            }
            
            // 第二行按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                QuickActionButton(
                    icon = Icons.Default.Security,
                    text = "权限管理",
                    onClick = onPermissions
                )
            }
        }
    }
}

/**
 * 快捷操作按钮
 */
@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = GoldAccent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = GoldAccent,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = DarkBrown,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 应用状态卡片
 */
@Composable
private fun AppStatusCard(
    isAudioServiceRunning: Boolean,
    totalAlarms: Int,
    enabledAlarms: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmWhite.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "应用状态",
                style = MaterialTheme.typography.titleSmall,
                color = DarkBrown,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    label = "音频服务",
                    value = if (isAudioServiceRunning) "运行中" else "已停止",
                    isPositive = isAudioServiceRunning
                )
                
                StatusItem(
                    label = "闹钟总数",
                    value = totalAlarms.toString(),
                    isPositive = totalAlarms > 0
                )
                
                StatusItem(
                    label = "已启用",
                    value = enabledAlarms.toString(),
                    isPositive = enabledAlarms > 0
                )
            }
        }
    }
}

/**
 * 状态项组件
 */
@Composable
private fun StatusItem(
    label: String,
    value: String,
    isPositive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (isPositive) GoldAccent else DarkBrown.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DarkBrown.copy(alpha = 0.7f)
        )
    }
}