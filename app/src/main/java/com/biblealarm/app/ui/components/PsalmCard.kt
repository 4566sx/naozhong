package com.biblealarm.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblealarm.app.data.model.Psalm
import com.biblealarm.app.ui.theme.DarkBrown
import com.biblealarm.app.ui.theme.GoldAccent
import com.biblealarm.app.ui.theme.WarmWhite

/**
 * 诗篇卡片组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsalmCard(
    psalm: Psalm?,
    isPlaying: Boolean = false,
    onPlayClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 顶部装饰线
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                GoldAccent.copy(alpha = 0.3f),
                                GoldAccent,
                                GoldAccent.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
            
            // 今日诗篇标题
            Text(
                text = "今日诗篇",
                style = MaterialTheme.typography.titleMedium,
                color = DarkBrown.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            
            // 诗篇信息
            if (psalm != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 诗篇编号
                    Text(
                        text = "诗篇 ${psalm.number}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // 诗篇标题
                    if (psalm.title.isNotEmpty()) {
                        Text(
                            text = psalm.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = DarkBrown,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // 诗篇预览文本
                    if (psalm.content.isNotEmpty()) {
                        Text(
                            text = psalm.content.take(100) + if (psalm.content.length > 100) "..." else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkBrown.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                // 加载状态
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = GoldAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "正在选择今日诗篇...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkBrown.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 播放控制按钮
            if (psalm != null) {
                PlaybackControls(
                    isPlaying = isPlaying,
                    isAvailable = psalm.isAvailable,
                    onPlayClick = onPlayClick,
                    onPauseClick = onPauseClick,
                    onNextClick = onNextClick,
                    onPreviousClick = onPreviousClick
                )
            }
        }
    }
}

/**
 * 播放控制组件
 */
@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    isAvailable: Boolean,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上一篇按钮
        IconButton(
            onClick = onPreviousClick,
            enabled = isAvailable
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "上一篇",
                tint = if (isAvailable) GoldAccent else GoldAccent.copy(alpha = 0.3f),
                modifier = Modifier.size(28.dp)
            )
        }
        
        // 播放/暂停按钮
        FloatingActionButton(
            onClick = if (isPlaying) onPauseClick else onPlayClick,
            modifier = Modifier.size(56.dp),
            containerColor = if (isAvailable) GoldAccent else GoldAccent.copy(alpha = 0.3f),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = when {
                    !isAvailable -> Icons.Default.MusicOff
                    isPlaying -> Icons.Default.Pause
                    else -> Icons.Default.PlayArrow
                },
                contentDescription = when {
                    !isAvailable -> "音频不可用"
                    isPlaying -> "暂停"
                    else -> "播放"
                },
                modifier = Modifier.size(32.dp)
            )
        }
        
        // 下一篇按钮
        IconButton(
            onClick = onNextClick,
            enabled = isAvailable
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "下一篇",
                tint = if (isAvailable) GoldAccent else GoldAccent.copy(alpha = 0.3f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
    
    // 播放状态提示
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                !isAvailable -> Icons.Default.ErrorOutline
                isPlaying -> Icons.Default.VolumeUp
                else -> Icons.Default.VolumeOff
            },
            contentDescription = null,
            tint = when {
                !isAvailable -> MaterialTheme.colorScheme.error
                isPlaying -> GoldAccent
                else -> DarkBrown.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = when {
                !isAvailable -> "音频不可用"
                isPlaying -> "正在播放"
                else -> "已暂停"
            },
            style = MaterialTheme.typography.bodySmall,
            color = when {
                !isAvailable -> MaterialTheme.colorScheme.error
                isPlaying -> GoldAccent
                else -> DarkBrown.copy(alpha = 0.5f)
            }
        )
    }
}

/**
 * 简化版诗篇卡片
 */
@Composable
fun SimplePsalmCard(
    psalm: Psalm?,
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
        if (psalm != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 诗篇图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "诗篇",
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // 诗篇信息
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "诗篇 ${psalm.number}",
                        style = MaterialTheme.typography.titleSmall,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (psalm.title.isNotEmpty()) {
                        Text(
                            text = psalm.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkBrown,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // 状态指示器
                Icon(
                    imageVector = if (psalm.isAvailable) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = if (psalm.isAvailable) "可用" else "不可用",
                    tint = if (psalm.isAvailable) Color.Green else Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            // 加载状态
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = GoldAccent,
                    strokeWidth = 2.dp
                )
                
                Text(
                    text = "正在加载诗篇...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkBrown.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 诗篇列表项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsalmListItem(
    psalm: Psalm,
    isSelected: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GoldAccent.copy(alpha = 0.1f) else WarmWhite
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, GoldAccent)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 诗篇编号
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) GoldAccent else GoldAccent.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = psalm.number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 诗篇信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = psalm.getDisplayTitle(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkBrown,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (psalm.content.isNotEmpty()) {
                    Text(
                        text = psalm.content.take(50) + if (psalm.content.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkBrown.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // 状态图标
            if (isPlaying) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "正在播放",
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
            } else if (!psalm.isAvailable) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "不可用",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}