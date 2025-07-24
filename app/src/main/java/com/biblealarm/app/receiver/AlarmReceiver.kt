package com.biblealarm.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.biblealarm.app.data.database.BibleAlarmDatabase
import com.biblealarm.app.manager.AlarmManager
import com.biblealarm.app.service.AlarmService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import java.util.*

/**
 * 闹钟广播接收器
 * 处理系统闹钟触发事件
 */
class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "收到闹钟广播: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // 系统启动后重新设置所有闹钟
                handleBootCompleted(context)
            }
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                // 时间或时区改变后重新设置闹钟
                handleTimeChanged(context)
            }
            else -> {
                // 处理闹钟触发
                handleAlarmTriggered(context, intent)
            }
        }
    }
    
    /**
     * 处理闹钟触发
     */
    private fun handleAlarmTriggered(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmManager.EXTRA_ALARM_ID, -1L)
        val psalmNumber = intent.getIntExtra(AlarmManager.EXTRA_PSALM_NUMBER, -1)
        
        if (alarmId == -1L) {
            Log.e(TAG, "无效的闹钟ID")
            return
        }
        
        Log.d(TAG, "闹钟触发: ID=$alarmId, 诗篇=$psalmNumber")
        
        // 启动闹钟服务处理触发逻辑
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmManager.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmManager.EXTRA_PSALM_NUMBER, psalmNumber)
            action = AlarmService.ACTION_ALARM_TRIGGERED
        }
        
        try {
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "启动闹钟服务失败", e)
            // 备用方案：直接启动闹钟活动
            startAlarmActivity(context, alarmId, psalmNumber)
        }
        
        // 如果是重复闹钟，重新设置下次触发
        scheduleNextRepeat(context, alarmId)
    }
    
    /**
     * 处理系统启动完成
     */
    private fun handleBootCompleted(context: Context) {
        Log.d(TAG, "系统启动完成，重新设置闹钟")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = BibleAlarmDatabase.getDatabase(context)
                val alarmDao = database.alarmDao()
                val alarmManager = com.biblealarm.app.manager.AlarmManager(context)
                
                // 获取所有启用的闹钟并重新设置
                alarmDao.getEnabledAlarms().collect { alarms ->
                    alarms.forEach { alarm ->
                        try {
                            alarmManager.setAlarm(alarm)
                            Log.d(TAG, "重新设置闹钟: ${alarm.getTimeString()}")
                        } catch (e: Exception) {
                            Log.e(TAG, "重新设置闹钟失败: ${alarm.id}", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理系统启动失败", e)
            }
        }
    }
    
    /**
     * 处理时间改变
     */
    private fun handleTimeChanged(context: Context) {
        Log.d(TAG, "时间或时区改变，重新设置闹钟")
        handleBootCompleted(context) // 使用相同的逻辑
    }
    
    /**
     * 重新安排重复闹钟的下次触发
     */
    private fun scheduleNextRepeat(context: Context, alarmId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = BibleAlarmDatabase.getDatabase(context)
                val alarmDao = database.alarmDao()
                val alarmManager = com.biblealarm.app.manager.AlarmManager(context)
                
                val alarm = alarmDao.getAlarmById(alarmId)
                if (alarm != null && alarm.isEnabled && alarm.repeatDays.isNotEmpty()) {
                    // 获取当前星期几
                    val currentDayOfWeek = getCurrentDayOfWeek()
                    
                    // 重新设置该星期几的下次触发
                    alarmManager.rescheduleRepeatingAlarm(alarm, currentDayOfWeek)
                    
                    // 更新最后触发时间
                    alarmDao.updateLastTriggered(alarmId, System.currentTimeMillis())
                    
                    Log.d(TAG, "重新安排重复闹钟: ${alarm.getTimeString()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "重新安排重复闹钟失败: $alarmId", e)
            }
        }
    }
    
    /**
     * 启动闹钟活动
     */
    private fun startAlarmActivity(context: Context, alarmId: Long, psalmNumber: Int) {
        val activityIntent = Intent().apply {
            setClassName(context, "com.biblealarm.app.ui.alarm.AlarmActivity")
            putExtra(AlarmManager.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmManager.EXTRA_PSALM_NUMBER, psalmNumber)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_CLEAR_TOP or
                   Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        try {
            context.startActivity(activityIntent)
        } catch (e: Exception) {
            Log.e(TAG, "启动闹钟活动失败", e)
        }
    }
    
    /**
     * 获取当前星期几
     */
    private fun getCurrentDayOfWeek(): DayOfWeek {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            else -> DayOfWeek.MONDAY
        }
    }
}