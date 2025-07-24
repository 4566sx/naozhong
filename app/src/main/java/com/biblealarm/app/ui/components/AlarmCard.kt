package com.biblealarm.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblealarm.app.data.model.Alarm
import com.biblealarm.app.ui.theme.DarkBrown
import com.biblealarm.app.ui.theme.GoldAccent
import com.biblealarm.app.ui.theme.WarmWhite

/**
 * 闹钟卡片组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggleEnabled: (Boolean) -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 顶部：时间和开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 闹钟时间
                Text(
                    text = alarm.getTimeString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (alarm.isEnabled) GoldAccent else DarkBrown.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
                
                // 开关
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GoldAccent,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = DarkBrown.copy(alpha = 0.3f)
                    )
                )
            }
            
            // 闹钟标签
            if (alarm.label.isNotEmpty()) {
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (alarm.isEnabled) DarkBrown else DarkBrown.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 重复设置
            if (alarm.repeatDays.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "重复",
                        tint = if (alarm.isEnabled) GoldAccent else GoldAccent.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = alarm.getRepeatText(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (alarm.isEnabled) DarkBrown else DarkBrown.copy(alpha = 0.5f)
                    )
                }
            }
            
            // 功能信息行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 功能标签
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 贪睡标签
                    if (alarm.isSnoozeEnabled) {
                        FeatureChip(
                            text = "贪睡 ${alarm.snoozeDuration}分钟",
                            icon = Icons.Default.Snooze,
                            enabled = alarm.isEnabled
                        )
                    }
                    
                    // 振动标签
                    if (alarm.isVibrateEnabled) {
                        FeatureChip(
                            text = "振动",
                            icon = Icons.Default.Vibration,
                            enabled = alarm.isEnabled
                        )
                    }
                }
                
                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = if (alarm.isEnabled) GoldAccent else GoldAccent.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error.copy(
                                alpha = if (alarm.isEnabled) 1f else 0.5f
                            ),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 简化版闹钟卡片
 */
@Composable
fun SimpleAlarmCard(
    alarm: Alarm,
    onToggleEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 闹钟信息
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = alarm.getTimeString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (alarm.isEnabled) GoldAccent else DarkBrown.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                
                if (alarm.label.isNotEmpty()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (alarm.isEnabled) DarkBrown else DarkBrown.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (alarm.repeatDays.isNotEmpty()) {
                    Text(
                        text = alarm.getRepeatText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (alarm.isEnabled) DarkBrown.copy(alpha = 0.7f) else DarkBrown.copy(alpha = 0.4f)
                    )
                }
            }
            
            // 开关
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggleEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GoldAccent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = DarkBrown.copy(alpha = 0.3f)
                )
            )
        }
    }
}

/**
 * 下一个闹钟卡片
 */
@Composable
fun NextAlarmCard(
    alarm: Alarm?,
    timeUntilAlarm: String = "",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GoldAccent.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (alarm != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 标题
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "下一个闹钟",
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "下一个闹钟",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 闹钟时间
                Text(
                    text = alarm.getTimeString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = DarkBrown,
                    fontWeight = FontWeight.Bold
                )
                
                // 闹钟标签
                if (alarm.label.isNotEmpty()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = DarkBrown,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // 倒计时
                if (timeUntilAlarm.isNotEmpty()) {
                    Text(
                        text = "还有 $timeUntilAlarm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkBrown.copy(alpha = 0.7f)
                    )
                }
                
                // 重复信息
                if (alarm.repeatDays.isNotEmpty()) {
                    Text(
                        text = alarm.getRepeatText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkBrown.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            // 无闹钟状态
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AlarmOff,
                    contentDescription = "无闹钟",
                    tint = DarkBrown.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "暂无设置的闹钟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkBrown.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 闹钟列表项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListItem(
    alarm: Alarm,
    onToggleEnabled: (Boolean) -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmWhite
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧信息
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 时间图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (alarm.isEnabled) GoldAccent.copy(alpha = 0.1f) 
                            else DarkBrown.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "闹钟",
                        tint = if (alarm.isEnabled) GoldAccent else DarkBrown.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // 闹钟信息
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = alarm.getTimeString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (alarm.isEnabled) DarkBrown else DarkBrown.copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    if (alarm.label.isNotEmpty()) {
                        Text(
                            text = alarm.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (alarm.isEnabled) DarkBrown.copy(alpha = 0.7f) else DarkBrown.copy(alpha = 0.4f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (alarm.repeatDays.isNotEmpty()) {
                        Text(
                            text = alarm.getRepeatText(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (alarm.isEnabled) GoldAccent else GoldAccent.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            // 右侧开关
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggleEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GoldAccent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = DarkBrown.copy(alpha = 0.3f)
                )
            )
        }
    }
}

/**
 * 功能标签组件
 */
@Composable
private fun FeatureChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) GoldAccent.copy(alpha = 0.1f) else DarkBrown.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) GoldAccent else GoldAccent.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) GoldAccent else GoldAccent.copy(alpha = 0.5f)
            )
        }
    }
}