package com.biblealarm.app.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.biblealarm.app.data.model.Alarm
import com.biblealarm.app.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.DayOfWeek
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 闹钟管理器
 * 负责系统闹钟的设置、取消和管理
 */
@Singleton
class AlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val systemAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        const val ALARM_REQUEST_CODE_BASE = 10000
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_PSALM_NUMBER = "psalm_number"
    }
    
    /**
     * 设置闹钟
     */
    fun setAlarm(alarm: Alarm) {
        if (!alarm.isEnabled) {
            cancelAlarm(alarm)
            return
        }
        
        if (alarm.repeatDays.isEmpty()) {
            // 一次性闹钟
            setOneTimeAlarm(alarm)
        } else {
            // 重复闹钟
            setRepeatingAlarm(alarm)
        }
    }
    
    /**
     * 设置一次性闹钟
     */
    private fun setOneTimeAlarm(alarm: Alarm) {
        val triggerTime = calculateNextTriggerTime(alarm)
        val pendingIntent = createAlarmPendingIntent(alarm)
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                systemAlarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                systemAlarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Android 12+ 需要精确闹钟权限
            throw AlarmPermissionException("需要精确闹钟权限才能设置闹钟")
        }
    }
    
    /**
     * 设置重复闹钟
     */
    private fun setRepeatingAlarm(alarm: Alarm) {
        // 为每个重复日期设置单独的闹钟
        alarm.repeatDays.forEach { dayOfWeek ->
            val triggerTime = calculateNextTriggerTimeForDay(alarm, dayOfWeek)
            val requestCode = getRequestCodeForDay(alarm.id, dayOfWeek)
            val pendingIntent = createAlarmPendingIntent(alarm, requestCode)
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    systemAlarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    systemAlarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                throw AlarmPermissionException("需要精确闹钟权限才能设置闹钟")
            }
        }
    }
    
    /**
     * 取消闹钟
     */
    fun cancelAlarm(alarm: Alarm) {
        if (alarm.repeatDays.isEmpty()) {
            // 取消一次性闹钟
            val pendingIntent = createAlarmPendingIntent(alarm)
            systemAlarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } else {
            // 取消重复闹钟的所有实例
            alarm.repeatDays.forEach { dayOfWeek ->
                val requestCode = getRequestCodeForDay(alarm.id, dayOfWeek)
                val pendingIntent = createAlarmPendingIntent(alarm, requestCode)
                systemAlarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
    
    /**
     * 创建闹钟PendingIntent
     */
    private fun createAlarmPendingIntent(alarm: Alarm, requestCode: Int? = null): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_PSALM_NUMBER, alarm.psalmNumber)
        }
        
        val code = requestCode ?: alarm.id.toInt()
        return PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * 计算下次触发时间
     */
    private fun calculateNextTriggerTime(alarm: Alarm): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val now = System.currentTimeMillis()
        
        // 如果设置的时间已经过了今天，则设置为明天
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis
    }
    
    /**
     * 计算指定星期几的下次触发时间
     */
    private fun calculateNextTriggerTimeForDay(alarm: Alarm, dayOfWeek: DayOfWeek): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val targetDayOfWeek = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> Calendar.SUNDAY
            DayOfWeek.MONDAY -> Calendar.MONDAY
            DayOfWeek.TUESDAY -> Calendar.TUESDAY
            DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
            DayOfWeek.THURSDAY -> Calendar.THURSDAY
            DayOfWeek.FRIDAY -> Calendar.FRIDAY
            DayOfWeek.SATURDAY -> Calendar.SATURDAY
        }
        
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysUntilTarget = (targetDayOfWeek - currentDayOfWeek + 7) % 7
        
        // 如果是今天且时间还没到，则今天触发；否则下周同一天触发
        val now = System.currentTimeMillis()
        if (daysUntilTarget == 0 && calendar.timeInMillis > now) {
            // 今天触发
        } else {
            // 计算到下次该星期几的天数
            val actualDaysUntil = if (daysUntilTarget == 0) 7 else daysUntilTarget
            calendar.add(Calendar.DAY_OF_YEAR, actualDaysUntil)
        }
        
        return calendar.timeInMillis
    }
    
    /**
     * 获取特定星期几的请求码
     */
    private fun getRequestCodeForDay(alarmId: Long, dayOfWeek: DayOfWeek): Int {
        val dayCode = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }
        return (ALARM_REQUEST_CODE_BASE + alarmId * 10 + dayCode).toInt()
    }
    
    /**
     * 重新设置重复闹钟的下次触发
     */
    fun rescheduleRepeatingAlarm(alarm: Alarm, triggeredDayOfWeek: DayOfWeek) {
        if (alarm.repeatDays.contains(triggeredDayOfWeek)) {
            val nextTriggerTime = calculateNextTriggerTimeForDay(alarm, triggeredDayOfWeek)
            val requestCode = getRequestCodeForDay(alarm.id, triggeredDayOfWeek)
            val pendingIntent = createAlarmPendingIntent(alarm, requestCode)
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    systemAlarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                } else {
                    systemAlarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                throw AlarmPermissionException("需要精确闹钟权限才能重新设置闹钟")
            }
        }
    }
    
    /**
     * 检查是否可以设置精确闹钟
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            systemAlarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    /**
     * 获取下次闹钟触发时间
     */
    fun getNextAlarmTime(alarm: Alarm): Long? {
        return if (alarm.isEnabled) {
            if (alarm.repeatDays.isEmpty()) {
                calculateNextTriggerTime(alarm)
            } else {
                // 找到最近的触发时间
                alarm.repeatDays.minOfOrNull { dayOfWeek ->
                    calculateNextTriggerTimeForDay(alarm, dayOfWeek)
                }
            }
        } else {
            null
        }
    }
    
    /**
     * 格式化闹钟触发时间显示
     */
    fun formatAlarmTime(triggerTime: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = triggerTime
        }
        
        val now = Calendar.getInstance()
        val diffInMillis = triggerTime - now.timeInMillis
        val diffInMinutes = diffInMillis / (1000 * 60)
        val diffInHours = diffInMinutes / 60
        val diffInDays = diffInHours / 24
        
        return when {
            diffInDays >= 1 -> "${diffInDays}天后"
            diffInHours >= 1 -> "${diffInHours}小时${diffInMinutes % 60}分钟后"
            diffInMinutes >= 1 -> "${diffInMinutes}分钟后"
            else -> "即将响起"
        }
    }
    
    /**
     * 检查闹钟是否已设置
     */
    fun isAlarmSet(alarm: Alarm): Boolean {
        return try {
            val pendingIntent = createAlarmPendingIntent(alarm)
            val isSet = pendingIntent.let { pi ->
                PendingIntent.getBroadcast(
                    context,
                    alarm.id.toInt(),
                    Intent(context, AlarmReceiver::class.java),
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                ) != null
            }
            isSet
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * 闹钟权限异常
 */
class AlarmPermissionException(message: String) : Exception(message)

/**
 * 闹钟设置结果
 */
sealed class AlarmSetResult {
    object Success : AlarmSetResult()
    data class Error(val message: String) : AlarmSetResult()
    object PermissionRequired : AlarmSetResult()
}