package com.biblealarm.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.datetime.LocalTime
import kotlinx.datetime.DayOfWeek

/**
 * 闹钟数据模型
 */
@Entity(tableName = "alarms")
@Parcelize
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                           // 闹钟ID
    val hour: Int,                              // 小时 (0-23)
    val minute: Int,                            // 分钟 (0-59)
    val isEnabled: Boolean = true,              // 是否启用
    val label: String = "",                     // 闹钟标签
    val repeatDays: Set<DayOfWeek> = emptySet(), // 重复日期
    val isSnoozeEnabled: Boolean = true,        // 是否启用贪睡
    val snoozeDuration: Int = 5,                // 贪睡时长(分钟)
    val volume: Float = 0.7f,                   // 音量 (0.0-1.0)
    val isVibrationEnabled: Boolean = true,     // 是否启用震动
    val psalmNumber: Int? = null,               // 指定诗篇编号(null表示随机)
    val createdAt: Long = System.currentTimeMillis(), // 创建时间
    val lastTriggered: Long? = null             // 最后触发时间
) : Parcelable {
    
    /**
     * 获取时间显示格式
     */
    fun getTimeString(): String {
        return String.format("%02d:%02d", hour, minute)
    }
    
    /**
     * 获取重复日期显示文本
     */
    fun getRepeatText(): String {
        return when {
            repeatDays.isEmpty() -> "仅一次"
            repeatDays.size == 7 -> "每天"
            repeatDays.containsAll(listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) 
                && repeatDays.size == 5 -> "工作日"
            repeatDays.containsAll(listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) 
                && repeatDays.size == 2 -> "周末"
            else -> {
                val dayNames = mapOf(
                    DayOfWeek.MONDAY to "周一",
                    DayOfWeek.TUESDAY to "周二", 
                    DayOfWeek.WEDNESDAY to "周三",
                    DayOfWeek.THURSDAY to "周四",
                    DayOfWeek.FRIDAY to "周五",
                    DayOfWeek.SATURDAY to "周六",
                    DayOfWeek.SUNDAY to "周日"
                )
                repeatDays.sortedBy { it.ordinal }
                    .joinToString(", ") { dayNames[it] ?: "" }
            }
        }
    }
    
    /**
     * 获取下次触发时间
     */
    fun getNextTriggerTime(): Long {
        // 这里需要根据当前时间和重复设置计算下次触发时间
        // 实际实现会在AlarmManager中处理
        return System.currentTimeMillis()
    }
    
    /**
     * 检查是否应该在指定日期触发
     */
    fun shouldTriggerOnDay(dayOfWeek: DayOfWeek): Boolean {
        return repeatDays.isEmpty() || repeatDays.contains(dayOfWeek)
    }
    
    /**
     * 获取音量百分比显示
     */
    fun getVolumePercentage(): Int {
        return (volume * 100).toInt()
    }
    
    companion object {
        /**
         * 创建默认闹钟
         */
        fun createDefault(): Alarm {
            return Alarm(
                hour = 7,
                minute = 0,
                label = "晨祷闹钟",
                repeatDays = setOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
                )
            )
        }
        
        /**
         * 预设闹钟模板
         */
        val presetAlarms = listOf(
            Alarm(hour = 6, minute = 0, label = "晨祷", repeatDays = setOf(DayOfWeek.SUNDAY)),
            Alarm(hour = 7, minute = 0, label = "工作日晨祷", repeatDays = setOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            )),
            Alarm(hour = 21, minute = 0, label = "晚祷", repeatDays = emptySet())
        )
    }
}

/**
 * 闹钟状态枚举
 */
enum class AlarmState {
    INACTIVE,    // 未激活
    ACTIVE,      // 激活等待
    TRIGGERED,   // 已触发
    SNOOZED     // 贪睡中
}