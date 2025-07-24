package com.biblealarm.app.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biblealarm.app.manager.AlarmManager
import com.biblealarm.app.ui.theme.BibleAlarmTheme
import com.biblealarm.app.ui.theme.DarkBrown
import com.biblealarm.app.ui.theme.GoldAccent
import com.biblealarm.app.ui.theme.WarmWhite
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * 闹钟触发全屏活动
 */
@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {
    
    private val viewModel: AlarmViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置全屏显示和锁屏上显示
        setupFullScreenAndLockScreen()
        
        // 获取传递的参数
        val alarmId = intent.getLongExtra(AlarmManager.EXTRA_ALARM_ID, -1L)
        val psalmNumber = intent.getIntExtra(AlarmManager.EXTRA_PSALM_NUMBER, -1)
        
        // 初始化ViewModel
        viewModel.initialize(alarmId, psalmNumber)
        
        setContent {
            BibleAlarmTheme {
                AlarmScreen(
                    viewModel = viewModel,
                    onDismiss = { finishAlarm() },
                    onSnooze = { 
                        viewModel.snoozeAlarm()
                        finishAlarm()
                    }
                )
            }
        }
    }
    
    /**
     * 设置全屏显示和锁屏上显示
     */
    private fun setupFullScreenAndLockScreen() {
        // 在锁屏上显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        
        // 全屏显示
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
    
    /**
     * 结束闹钟活动
     */
    private fun finishAlarm() {
        viewModel.stopAlarm()
        finish()
    }
    
    override fun onBackPressed() {
        // 防止用户通过返回键关闭闹钟
        // 必须通过关闭或贪睡按钮来处理
    }
    
    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanup()
    }
}

/**
 * 闹钟界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    viewModel: AlarmViewModel,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 自动更新时间
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateCurrentTime()
            delay(1000)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBrown),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 顶部时间显示
            TimeDisplay(currentTime = currentTime)
            
            // 中间闹钟信息
            AlarmInfoSection(
                alarmLabel = uiState.alarm?.label ?: "闹钟",
                psalmTitle = uiState.psalm?.getDisplayTitle() ?: "诗篇",
                isPlaying = uiState.isPlaying
            )
            
            // 底部控制按钮
            AlarmControlButtons(
                canSnooze = uiState.alarm?.isSnoozeEnabled == true,
                snoozeMinutes = uiState.alarm?.snoozeDuration ?: 5,
                onDismiss = onDismiss,
                onSnooze = onSnooze
            )
        }
        
        // 错误提示
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * 时间显示组件
 */
@Composable
private fun TimeDisplay(currentTime: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = currentTime,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = WarmWhite,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINESE).format(Date()),
            fontSize = 16.sp,
            color = WarmWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 闹钟信息区域
 */
@Composable
private fun AlarmInfoSection(
    alarmLabel: String,
    psalmTitle: String,
    isPlaying: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmWhite.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 闹钟图标
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(GoldAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "闹钟",
                    tint = GoldAccent,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // 闹钟标签
            Text(
                text = alarmLabel,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBrown,
                textAlign = TextAlign.Center
            )
            
            // 诗篇信息
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "今日诗篇",
                    fontSize = 14.sp,
                    color = DarkBrown.copy(alpha = 0.7f)
                )
                
                Text(
                    text = psalmTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoldAccent,
                    textAlign = TextAlign.Center
                )
            }
            
            // 播放状态指示器
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (isPlaying) "正在播放" else "未播放",
                    tint = if (isPlaying) GoldAccent else DarkBrown.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = if (isPlaying) "正在播放" else "音频准备中",
                    fontSize = 14.sp,
                    color = if (isPlaying) GoldAccent else DarkBrown.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 闹钟控制按钮
 */
@Composable
private fun AlarmControlButtons(
    canSnooze: Boolean,
    snoozeMinutes: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 贪睡按钮
        if (canSnooze) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = onSnooze,
                    modifier = Modifier.size(72.dp),
                    containerColor = WarmWhite.copy(alpha = 0.9f),
                    contentColor = GoldAccent
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "贪睡",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Text(
                    text = "贪睡 ${snoozeMinutes}分钟",
                    fontSize = 12.sp,
                    color = WarmWhite.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // 关闭按钮
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = onDismiss,
                modifier = Modifier.size(72.dp),
                containerColor = GoldAccent,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "关闭",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Text(
                text = "关闭闹钟",
                fontSize = 12.sp,
                color = WarmWhite.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}