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
import androidx.compose.ui.unit.dp
import com.biblealarm.app.ui.theme.DarkBrown
import com.biblealarm.app.ui.theme.GoldAccent
import com.biblealarm.app.ui.theme.WarmWhite

/**
 * 音量控制组件
 */
@Composable
fun VolumeControl(
    volume: Float,
    isMuted: Boolean = false,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit,
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
            // 标题和静音按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "音量控制",
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkBrown,
                    fontWeight = FontWeight.SemiBold
                )
                
                IconButton(
                    onClick = onMuteToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMuted) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            else GoldAccent.copy(alpha = 0.1f)
                        )
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else getVolumeIcon(volume),
                        contentDescription = if (isMuted) "取消静音" else "静音",
                        tint = if (isMuted) MaterialTheme.colorScheme.error else GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // 音量滑块
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = if (isMuted) 0f else volume,
                    onValueChange = { newVolume ->
                        if (isMuted && newVolume > 0f) {
                            onMuteToggle() // 自动取消静音
                        }
                        onVolumeChange(newVolume)
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldAccent,
                        activeTrackColor = GoldAccent,
                        inactiveTrackColor = GoldAccent.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 音量百分比和描述
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "0%",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkBrown.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = if (isMuted) "静音" else "${(volume * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isMuted) MaterialTheme.colorScheme.error else GoldAccent,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "100%",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkBrown.copy(alpha = 0.6f)
                    )
                }
            }
            
            // 音量描述
            Text(
                text = getVolumeDescription(volume, isMuted),
                style = MaterialTheme.typography.bodySmall,
                color = DarkBrown.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 简化版音量控制
 */
@Composable
fun SimpleVolumeControl(
    volume: Float,
    isMuted: Boolean = false,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 静音按钮
        IconButton(
            onClick = onMuteToggle,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else getVolumeIcon(volume),
                contentDescription = if (isMuted) "取消静音" else "静音",
                tint = if (isMuted) MaterialTheme.colorScheme.error else GoldAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // 音量滑块
        Slider(
            value = if (isMuted) 0f else volume,
            onValueChange = { newVolume ->
                if (isMuted && newVolume > 0f) {
                    onMuteToggle()
                }
                onVolumeChange(newVolume)
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = GoldAccent,
                activeTrackColor = GoldAccent,
                inactiveTrackColor = GoldAccent.copy(alpha = 0.3f)
            ),
            modifier = Modifier.weight(1f)
        )
        
        // 音量百分比
        Text(
            text = if (isMuted) "静音" else "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = if (isMuted) MaterialTheme.colorScheme.error else DarkBrown,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(40.dp)
        )
    }
}

/**
 * 音量快捷控制按钮
 */
@Composable
fun VolumeQuickControls(
    volume: Float,
    isMuted: Boolean = false,
    onVolumeIncrease: () -> Unit,
    onVolumeDecrease: () -> Unit,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 减小音量
        IconButton(
            onClick = onVolumeDecrease,
            enabled = !isMuted && volume > 0f
        ) {
            Icon(
                imageVector = Icons.Default.VolumeDown,
                contentDescription = "减小音量",
                tint = if (!isMuted && volume > 0f) GoldAccent else GoldAccent.copy(alpha = 0.3f)
            )
        }
        
        // 静音切换
        IconButton(
            onClick = onMuteToggle,
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (isMuted) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    else GoldAccent.copy(alpha = 0.1f)
                )
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else getVolumeIcon(volume),
                contentDescription = if (isMuted) "取消静音" else "静音",
                tint = if (isMuted) MaterialTheme.colorScheme.error else GoldAccent
            )
        }
        
        // 增大音量
        IconButton(
            onClick = onVolumeIncrease,
            enabled = !isMuted && volume < 1f
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "增大音量",
                tint = if (!isMuted && volume < 1f) GoldAccent else GoldAccent.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * 音量指示器
 */
@Composable
fun VolumeIndicator(
    volume: Float,
    isMuted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isMuted) Icons.Default.VolumeOff else getVolumeIcon(volume),
            contentDescription = "音量",
            tint = if (isMuted) MaterialTheme.colorScheme.error else GoldAccent,
            modifier = Modifier.size(16.dp)
        )
        
        Text(
            text = if (isMuted) "静音" else "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = if (isMuted) MaterialTheme.colorScheme.error else DarkBrown
        )
    }
}

/**
 * 音量波形指示器
 */
@Composable
fun VolumeWaveIndicator(
    volume: Float,
    isMuted: Boolean = false,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // 音量图标
        Icon(
            imageVector = if (isMuted) Icons.Default.VolumeOff else getVolumeIcon(volume),
            contentDescription = "音量",
            tint = if (isMuted) MaterialTheme.colorScheme.error else GoldAccent,
            modifier = Modifier.size(16.dp)
        )
        
        // 波形指示器
        if (isPlaying && !isMuted) {
            repeat(5) { index ->
                val barHeight = when {
                    volume == 0f -> 2.dp
                    index < (volume * 5).toInt() -> (4 + index * 2).dp
                    else -> 2.dp
                }
                
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(barHeight)
                        .background(
                            color = if (index < (volume * 5).toInt()) GoldAccent else GoldAccent.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

/**
 * 根据音量获取对应的图标
 */
private fun getVolumeIcon(volume: Float) = when {
    volume == 0f -> Icons.Default.VolumeOff
    volume <= 0.3f -> Icons.Default.VolumeDown
    volume <= 0.7f -> Icons.Default.VolumeUp
    else -> Icons.Default.VolumeUp
}

/**
 * 获取音量描述文本
 */
private fun getVolumeDescription(volume: Float, isMuted: Boolean): String {
    return when {
        isMuted -> "音频已静音"
        volume == 0f -> "音量为零"
        volume <= 0.25f -> "低音量 - 适合安静环境"
        volume <= 0.5f -> "中等音量 - 日常使用"
        volume <= 0.75f -> "较高音量 - 嘈杂环境"
        else -> "最大音量 - 请注意保护听力"
    }
}