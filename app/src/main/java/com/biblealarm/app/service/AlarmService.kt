package com.biblealarm.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.biblealarm.app.MainActivity
import com.biblealarm.app.R
import com.biblealarm.app.data.database.BibleAlarmDatabase
import com.biblealarm.app.manager.AlarmManager
import com.biblealarm.app.manager.PsalmSelectionManager
import com.biblealarm.app.ui.alarm.AlarmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 闹钟服务
 * 处理闹钟触发后的业务逻辑
 */
class AlarmService : Service() {
    
    companion object {
        private const val TAG = "AlarmService"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "alarm_service_channel"
        
        const val ACTION_ALARM_TRIGGERED = "com.biblealarm.app.ACTION_ALARM_TRIGGERED"
        const val ACTION_SNOOZE_ALARM = "com.biblealarm.app.ACTION_SNOOZE_ALARM"
        const val ACTION_DISMISS_ALARM = "com.biblealarm.app.ACTION_DISMISS_ALARM"
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var database: BibleAlarmDatabase
    private lateinit var psalmSelectionManager: PsalmSelectionManager
    
    override fun onCreate() {
        super.onCreate()
        database = BibleAlarmDatabase.getDatabase(this)
        // 注意：这里需要手动创建PsalmSelectionManager，因为Service不支持Hilt注入
        // 在实际项目中，可以通过其他方式获取依赖
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_ALARM_TRIGGERED -> {
                handleAlarmTriggered(intent)
            }
            ACTION_SNOOZE_ALARM -> {
                handleSnoozeAlarm(intent)
            }
            ACTION_DISMISS_ALARM -> {
                handleDismissAlarm(intent)
            }
        }
        
        return START_NOT_STICKY
    }
    
    /**
     * 处理闹钟触发
     */
    private fun handleAlarmTriggered(intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmManager.EXTRA_ALARM_ID, -1L)
        val psalmNumber = intent.getIntExtra(AlarmManager.EXTRA_PSALM_NUMBER, -1)
        
        Log.d(TAG, "处理闹钟触发: ID=$alarmId, 诗篇=$psalmNumber")
        
        serviceScope.launch {
            try {
                val alarm = database.alarmDao().getAlarmById(alarmId)
                if (alarm == null) {
                    Log.e(TAG, "找不到闹钟: $alarmId")
                    stopSelf()
                    return@launch
                }
                
                // 更新最后触发时间
                database.alarmDao().updateLastTriggered(alarmId, System.currentTimeMillis())
                
                // 选择要播放的诗篇
                val selectedPsalm = if (psalmNumber > 0) {
                    // 使用指定的诗篇
                    database.psalmDao().getPsalmByNumber(psalmNumber)
                } else {
                    // 随机选择诗篇
                    database.psalmDao().getRandomPsalm()
                }
                
                if (selectedPsalm == null || !selectedPsalm.isAvailable) {
                    Log.e(TAG, "没有可用的诗篇音频")
                    showErrorNotification("没有可用的诗篇音频")
                    stopSelf()
                    return@launch
                }
                
                // 启动闹钟界面
                startAlarmActivity(alarmId, selectedPsalm.number)
                
                // 显示前台通知
                startForeground(NOTIFICATION_ID, createAlarmNotification(alarm.label, selectedPsalm.getDisplayTitle()))
                
                Log.d(TAG, "闹钟触发处理完成: ${selectedPsalm.getDisplayTitle()}")
                
            } catch (e: Exception) {
                Log.e(TAG, "处理闹钟触发失败", e)
                showErrorNotification("闹钟触发失败: ${e.message}")
                stopSelf()
            }
        }
    }
    
    /**
     * 处理贪睡闹钟
     */
    private fun handleSnoozeAlarm(intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmManager.EXTRA_ALARM_ID, -1L)
        
        Log.d(TAG, "处理贪睡闹钟: $alarmId")
        
        serviceScope.launch {
            try {
                val alarm = database.alarmDao().getAlarmById(alarmId)
                if (alarm == null) {
                    Log.e(TAG, "找不到闹钟: $alarmId")
                    stopSelf()
                    return@launch
                }
                
                if (alarm.isSnoozeEnabled) {
                    // 设置贪睡闹钟
                    val snoozeTime = System.currentTimeMillis() + (alarm.snoozeDuration * 60 * 1000)
                    val alarmManager = android.app.AlarmManager.getInstance(this@AlarmService)
                    
                    val snoozeIntent = Intent(this@AlarmService, AlarmService::class.java).apply {
                        action = ACTION_ALARM_TRIGGERED
                        putExtra(AlarmManager.EXTRA_ALARM_ID, alarmId)
                    }
                    
                    val snoozePendingIntent = PendingIntent.getService(
                        this@AlarmService,
                        (alarmId + 50000).toInt(), // 使用不同的请求码避免冲突
                        snoozeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            snoozeTime,
                            snoozePendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            android.app.AlarmManager.RTC_WAKEUP,
                            snoozeTime,
                            snoozePendingIntent
                        )
                    }
                    
                    showSnoozeNotification(alarm.snoozeDuration)
                    Log.d(TAG, "设置贪睡闹钟: ${alarm.snoozeDuration}分钟后")
                }
                
                stopSelf()
                
            } catch (e: Exception) {
                Log.e(TAG, "处理贪睡闹钟失败", e)
                stopSelf()
            }
        }
    }
    
    /**
     * 处理关闭闹钟
     */
    private fun handleDismissAlarm(intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmManager.EXTRA_ALARM_ID, -1L)
        
        Log.d(TAG, "关闭闹钟: $alarmId")
        
        // 取消所有相关的通知和服务
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()
        
        stopSelf()
    }
    
    /**
     * 启动闹钟活动
     */
    private fun startAlarmActivity(alarmId: Long, psalmNumber: Int) {
        val activityIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra(AlarmManager.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmManager.EXTRA_PSALM_NUMBER, psalmNumber)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_CLEAR_TOP or
                   Intent.FLAG_ACTIVITY_SINGLE_TOP or
                   Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        
        try {
            startActivity(activityIntent)
        } catch (e: Exception) {
            Log.e(TAG, "启动闹钟活动失败", e)
        }
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "闹钟服务",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "闹钟触发服务通知"
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建闹钟通知
     */
    private fun createAlarmNotification(alarmLabel: String, psalmTitle: String): Notification {
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("闹钟响起")
            .setContentText("$alarmLabel - $psalmTitle")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }
    
    /**
     * 显示贪睡通知
     */
    private fun showSnoozeNotification(snoozeMinutes: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("闹钟已贪睡")
            .setContentText("将在 $snoozeMinutes 分钟后再次响起")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    /**
     * 显示错误通知
     */
    private fun showErrorNotification(message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("闹钟错误")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "闹钟服务销毁")
    }
}

/**
 * AlarmManager扩展函数
 */
private fun android.app.AlarmManager.Companion.getInstance(context: android.content.Context): android.app.AlarmManager {
    return context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
}