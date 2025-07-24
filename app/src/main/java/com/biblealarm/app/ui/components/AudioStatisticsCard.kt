package com.biblealarm.app.ui.components

import androidx.compose.foundation.layout.*
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
import com.biblealarm.app.data.repository.AudioStatistics
import com.biblealarm.app.ui.theme.DarkBrown
import com.biblealarm.app.ui.theme.GoldAccent
import com.biblealarm.app.ui.theme.WarmWhite

/**
 * 音频统计信息卡片
 */
@Composable
fun AudioStatisticsCard(
    statistics: AudioStatistics,
    onRefreshClick: () -> Unit,
    onScanUserAudioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "音频资源统计",
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkBrown,
                    fontWeight = FontWeight.SemiBold
                )
                
                IconButton(onClick = onRefreshClick) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新统计",
                        tint = GoldAccent
                    )
                }
            }
            
            // 总体统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    icon = Icons.Default.LibraryMusic,
                    label = "总诗篇",
                    value = statistics.totalPsalms.toString(),
                    color = DarkBrown
                )
                
                StatisticItem(
                    icon = Icons.Default.CheckCircle,
                    label = "可用",
                    value = statistics.availablePsalms.toString(),
                    color = GoldAccent
                )
                
                StatisticItem(
                    icon = Icons.Default.Cancel,
                    label = "不可用",
                    value = statistics.unavailableCount.toString(),
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            // 可用性进度条
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "可用性",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkBrown
                    )
                    Text(
                        text = "${statistics.availabilityPercentage.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                LinearProgressIndicator(
                    progress = statistics.availabilityPercentage / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = GoldAccent,
                    trackColor = GoldAccent.copy(alpha = 0.3f)
                )
            }
            
            // 音频源分布
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AudioSourceItem(
                    icon = Icons.Default.CloudDone,
                    label = "内置音频",
                    count = statistics.builtInAudioCount,
                    color = MaterialTheme.colorScheme.primary
                )
                
                AudioSourceItem(
                    icon = Icons.Default.Person,
                    label = "用户音频",
                    count = statistics.userAudioCount,
                    color = GoldAccent
                )
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onScanUserAudioClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GoldAccent
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(GoldAccent)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("扫描用户音频")
                }
            }
        }
    }
}

/**
 * 统计项组件
 */
@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
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
 * 音频源项组件
 */
@Composable
private fun AudioSourceItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        
        Column {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DarkBrown.copy(alpha = 0.7f)
            )
        }
    }
}