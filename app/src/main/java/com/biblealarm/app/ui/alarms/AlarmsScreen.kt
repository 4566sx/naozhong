package com.biblealarm.app.ui.alarms

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biblealarm.app.data.model.Alarm
import com.biblealarm.app.ui.components.AlarmCard
import com.biblealarm.app.ui.components.AlarmListItem
import com.biblealarm.app.ui.theme.DarkBrown
import com.biblealarm.app.ui.theme.GoldAccent
import com.biblealarm.app.ui.theme.WarmWhite

/**
 * 闹钟管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddAlarm: () -> Unit,
    onNavigateToEditAlarm: (Long) -> Unit,
    viewModel: AlarmsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<Alarm?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadAlarms()
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
                    text = "闹钟管理",
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
            actions = {
                IconButton(onClick = onNavigateToAddAlarm) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加闹钟",
                        tint = GoldAccent
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = WarmWhite
            )
        )
        
        if (uiState.isLoading) {
            // 加载状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = GoldAccent,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "正在加载闹钟...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkBrown.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (uiState.alarms.isEmpty()) {
            // 空状态
            EmptyAlarmsState(
                onAddAlarm = onNavigateToAddAlarm
            )
        } else {
            // 闹钟列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 统计信息卡片
                item {
                    AlarmStatsCard(
                        totalAlarms = uiState.alarms.size,
                        enabledAlarms = uiState.alarms.count { it.isEnabled },
                        nextAlarm = uiState.nextAlarm
                    )
                }
                
                // 闹钟列表
                items(
                    items = uiState.alarms,
                    key = { it.id }
                ) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggleEnabled = { enabled ->
                            viewModel.toggleAlarm(alarm.id, enabled)
                        },
                        onEdit = {
                            onNavigateToEditAlarm(alarm.id)
                        },
                        onDelete = {
                            showDeleteDialog = alarm
                        }
                    )
                }
                
                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // 错误提示
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                // 显示错误提示
            }
        }
    }
    
    // 浮动添加按钮
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onNavigateToAddAlarm,
            modifier = Modifier.padding(16.dp),
            containerColor = GoldAccent,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加闹钟",
                modifier = Modifier.size(24.dp)
            )
        }
    }
    
    // 删除确认对话框
    showDeleteDialog?.let { alarm ->
        DeleteAlarmDialog(
            alarm = alarm,
            onConfirm = {
                viewModel.deleteAlarm(alarm.id)
                showDeleteDialog = null
            },
            onDismiss = {
                showDeleteDialog = null
            }
        )
    }
}

/**
 * 空状态组件
 */
@Composable
private fun EmptyAlarmsState(
    onAddAlarm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 空状态图标
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = GoldAccent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(60.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AlarmOff,
                contentDescription = "无闹钟",
                tint = GoldAccent,
                modifier = Modifier.size(60.dp)
            )
        }
        
        // 提示文本
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "暂无闹钟",
                style = MaterialTheme.typography.headlineSmall,
                color = DarkBrown,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "添加您的第一个圣经闹钟，\n让诗篇伴您开始美好的一天",
                style = MaterialTheme.typography.bodyMedium,
                color = DarkBrown.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        // 添加按钮
        Button(
            onClick = onAddAlarm,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "添加闹钟",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 闹钟统计卡片
 */
@Composable
private fun AlarmStatsCard(
    totalAlarms: Int,
    enabledAlarms: Int,
    nextAlarm: Alarm?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GoldAccent.copy(alpha = 0.1f)
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
                text = "闹钟概览",
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccent,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "总数",
                    value = totalAlarms.toString(),
                    icon = Icons.Default.Schedule
                )
                
                StatItem(
                    label = "已启用",
                    value = enabledAlarms.toString(),
                    icon = Icons.Default.Alarm
                )
                
                StatItem(
                    label = "已禁用",
                    value = (totalAlarms - enabledAlarms).toString(),
                    icon = Icons.Default.AlarmOff
                )
            }
            
            if (nextAlarm != null) {
                Divider(color = GoldAccent.copy(alpha = 0.3f))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "下一个闹钟",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "下一个闹钟：${nextAlarm.getTimeString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkBrown,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 统计项组件
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = GoldAccent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = DarkBrown,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DarkBrown.copy(alpha = 0.7f)
        )
    }
}

/**
 * 删除闹钟确认对话框
 */
@Composable
private fun DeleteAlarmDialog(
    alarm: Alarm,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "删除闹钟",
                color = DarkBrown,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "确定要删除这个闹钟吗？",
                    color = DarkBrown
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = alarm.getTimeString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = DarkBrown,
                            fontWeight = FontWeight.Bold
                        )
                        if (alarm.label.isNotEmpty()) {
                            Text(
                                text = alarm.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkBrown.copy(alpha = 0.8f)
                            )
                        }
                        if (alarm.repeatDays.isNotEmpty()) {
                            Text(
                                text = alarm.getRepeatText(),
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkBrown.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "删除",
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