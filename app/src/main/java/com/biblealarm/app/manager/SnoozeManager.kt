package com.biblealarm.app.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.biblealarm.app.data.model.Alarm
import com.biblealarm.app.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 贪睡管理器
 * 负责管理闹钟的贪睡功能
 */
@Singleton
class SnoozeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "SnoozeManager"
        private const val PREFS_NAME = "snooze_prefs"
        private const val KEY_SNOOZE_ALARMS = "snooze_alarms"
        private const val SNOOZE_REQUEST_CODE_BASE = 20000
        
        // 默认贪睡设置
        const val DEFAULT_SNOOZE_DURATION = 5 // 分钟
        const val MIN_SNOOZE_DURATION = 1
        const val MAX_SNOOZE_DURATION = 60
        const val MAX_SNOOZE_COUNT = 10 // 最大贪睡次数
    }
    
    private val systemAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // 当前贪睡状态
    private val _snoozeStates = MutableStateFlow<Map<Long, SnoozeState>>(emptyMap())
    val snoozeStates: StateFlow<Map<Long, SnoozeState>> = _snoozeStates.asStateFlow()
    
    // 贪睡历史
    private val _snoozeHistory = MutableStateFlow<List<SnoozeRecord>>(emptyList())
    val snoozeHistory: StateFlow<List<SnoozeRecord>> = _snoozeHistory.asStateFlow()
    
    init {
        loadSnoozeStates()
    }
    
    /**
     * 设置贪睡闹钟
     */
    fun snoozeAlarm(alarm: Alarm): SnoozeResult {
        return try {
            if (!alarm.isSnoozeEnabled) {
                return SnoozeResult.Error("该闹钟未启用贪睡功能")
            }
            
            val currentState = _snoozeStates.value[alarm.id]
            val snoozeCount = currentState?.snoozeCount ?: 0
            
            // 检查贪睡次数限制
            if (snoozeCount >= MAX_SNOOZE_COUNT) {
                return SnoozeResult.Error("已达到最大贪睡次数限制")
            }
            
            // 计算贪睡时间
            val snoozeTime = System.currentTimeMillis() + (alarm.snoozeDuration * 60 * 1000L)
            
            // 设置系统闹钟
            val success = setSnoozeAlarm(alarm, snoozeTime)
            if (!success) {
                return SnoozeResult.Error("设置贪睡闹钟失败")
            }
            
            // 更新贪睡状态
            val newState = SnoozeState(
                alarmId = alarm.id,
                snoozeTime = snoozeTime,
                snoozeCount = snoozeCount + 1,
                originalTriggerTime = currentState?.originalTriggerTime ?: System.currentTimeMillis(),
                isActive = true
            )
            
            updateSnoozeState(alarm.id, newState)
            
            // 记录贪睡历史
            addSnoozeRecord(alarm.id, snoozeTime, alarm.snoozeDuration)
            
            Log.d(TAG, "设置贪睡成功: 闹钟${alarm.id}, ${alarm.snoozeDuration}分钟后响起")
            
            SnoozeResult.Success(snoozeTime, snoozeCount + 1)
            
        } catch (e: Exception) {
            Log.e(TAG, "设置贪睡失败", e)
            SnoozeResult.Error("设置贪睡失败: ${e.message}")
        }
    }
    
    /**
     * 取消贪睡
     */
    fun cancelSnooze(alarmId: Long): Boolean {
        return try {
            val snoozeState = _snoozeStates.value[alarmId]
            if (snoozeState?.isActive == true) {
                // 取消系统闹钟
                cancelSnoozeAlarm(alarmId)
                
                // 更新状态
                val updatedState = snoozeState.copy(isActive = false)
                updateSnoozeState(alarmId, updatedState)
                
                Log.d(TAG, "取消贪睡成功: 闹钟$alarmId")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "取消贪睡失败", e)
            false
        }
    }
    
    /**
     * 清除闹钟的贪睡状态
     */
    fun clearSnoozeState(alarmId: Long) {
        try {
            cancelSnooze(alarmId)
            
            val currentStates = _snoozeStates.value.toMutableMap()
            currentStates.remove(alarmId)
            _snoozeStates.value = currentStates
            
            saveSnoozeStates()
            
            Log.d(TAG, "清除贪睡状态: 闹钟$alarmId")
        } catch (e: Exception) {
            Log.e(TAG, "清除贪睡状态失败", e)
        }
    }
    
    /**
     * 获取闹钟的贪睡状态
     */
    fun getSnoozeState(alarmId: Long): SnoozeState? {
        return _snoozeStates.value[alarmId]
    }
    
    /**
     * 检查闹钟是否在贪睡中
     */
    fun isAlarmSnoozed(alarmId: Long): Boolean {
        return _snoozeStates.value[alarmId]?.isActive == true
    }
    
    /**
     * 获取贪睡剩余时间
     */
    fun getSnoozeRemainingTime(alarmId: Long): Long {
        val snoozeState = _snoozeStates.value[alarmId]
        return if (snoozeState?.isActive == true) {
            maxOf(0, snoozeState.snoozeTime - System.currentTimeMillis())
        } else {
            0
        }
    }
    
    /**
     * 格式化贪睡剩余时间
     */
    fun formatSnoozeRemainingTime(alarmId: Long): String {
        val remainingTime = getSnoozeRemainingTime(alarmId)
        if (remainingTime <= 0) return "即将响起"
        
        val minutes = remainingTime / (1000 * 60)
        val seconds = (remainingTime % (1000 * 60)) / 1000
        
        return when {
            minutes > 0 -> "${minutes}分${seconds}秒后响起"
            else -> "${seconds}秒后响起"
        }
    }
    
    /**
     * 获取贪睡统计信息
     */
    fun getSnoozeStats(alarmId: Long): SnoozeStats {
        val snoozeState = _snoozeStates.value[alarmId]
        val history = _snoozeHistory.value.filter { it.alarmId == alarmId }
        
        return SnoozeStats(
            currentSnoozeCount = snoozeState?.snoozeCount ?: 0,
            totalSnoozeCount = history.size,
            averageSnoozeDuration = if (history.isNotEmpty()) {
                history.map { it.snoozeDuration }.average().toInt()
            } else 0,
            lastSnoozeTime = history.maxByOrNull { it.snoozeTime }?.snoozeTime,
            isCurrentlySnoozed = snoozeState?.isActive == true
        )
    }
    
    /**
     * 设置系统贪睡闹钟
     */
    private fun setSnoozeAlarm(alarm: Alarm, snoozeTime: Long): Boolean {
        return try {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", alarm.id)
                putExtra("is_snooze", true)
            }
            
            val requestCode = SNOOZE_REQUEST_CODE_BASE + alarm.id.toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                systemAlarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            } else {
                systemAlarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "设置系统贪睡闹钟失败", e)
            false
        }
    }
    
    /**
     * 取消系统贪睡闹钟
     */
    private fun cancelSnoozeAlarm(alarmId: Long) {
        try {
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = SNOOZE_REQUEST_CODE_BASE + alarmId.toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            systemAlarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "取消系统贪睡闹钟失败", e)
        }
    }
    
    /**
     * 更新贪睡状态
     */
    private fun updateSnoozeState(alarmId: Long, state: SnoozeState) {
        val currentStates = _snoozeStates.value.toMutableMap()
        currentStates[alarmId] = state
        _snoozeStates.value = currentStates
        
        saveSnoozeStates()
    }
    
    /**
     * 添加贪睡记录
     */
    private fun addSnoozeRecord(alarmId: Long, snoozeTime: Long, snoozeDuration: Int) {
        val record = SnoozeRecord(
            alarmId = alarmId,
            snoozeTime = snoozeTime,
            snoozeDuration = snoozeDuration,
            timestamp = System.currentTimeMillis()
        )
        
        val currentHistory = _snoozeHistory.value.toMutableList()
        currentHistory.add(record)
        
        // 只保留最近100条记录
        if (currentHistory.size > 100) {
            currentHistory.removeAt(0)
        }
        
        _snoozeHistory.value = currentHistory
        saveSnoozeHistory()
    }
    
    /**
     * 保存贪睡状态到本地
     */
    private fun saveSnoozeStates() {
        try {
            val statesJson = _snoozeStates.value.values.joinToString("|") { state ->
                "${state.alarmId},${state.snoozeTime},${state.snoozeCount},${state.originalTriggerTime},${state.isActive}"
            }
            prefs.edit().putString("snooze_states", statesJson).apply()
        } catch (e: Exception) {
            Log.e(TAG, "保存贪睡状态失败", e)
        }
    }
    
    /**
     * 从本地加载贪睡状态
     */
    private fun loadSnoozeStates() {
        try {
            val statesJson = prefs.getString("snooze_states", "") ?: ""
            if (statesJson.isNotEmpty()) {
                val states = statesJson.split("|").mapNotNull { stateStr ->
                    try {
                        val parts = stateStr.split(",")
                        if (parts.size == 5) {
                            val alarmId = parts[0].toLong()
                            val snoozeTime = parts[1].toLong()
                            val snoozeCount = parts[2].toInt()
                            val originalTriggerTime = parts[3].toLong()
                            val isActive = parts[4].toBoolean()
                            
                            // 检查贪睡是否已过期
                            val isStillActive = isActive && snoozeTime > System.currentTimeMillis()
                            
                            alarmId to SnoozeState(
                                alarmId = alarmId,
                                snoozeTime = snoozeTime,
                                snoozeCount = snoozeCount,
                                originalTriggerTime = originalTriggerTime,
                                isActive = isStillActive
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }.toMap()
                
                _snoozeStates.value = states
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载贪睡状态失败", e)
        }
    }
    
    /**
     * 保存贪睡历史
     */
    private fun saveSnoozeHistory() {
        try {
            val historyJson = _snoozeHistory.value.joinToString("|") { record ->
                "${record.alarmId},${record.snoozeTime},${record.snoozeDuration},${record.timestamp}"
            }
            prefs.edit().putString("snooze_history", historyJson).apply()
        } catch (e: Exception) {
            Log.e(TAG, "保存贪睡历史失败", e)
        }
    }
    
    /**
     * 清理过期的贪睡状态
     */
    fun cleanupExpiredSnoozes() {
        val currentTime = System.currentTimeMillis()
        val currentStates = _snoozeStates.value.toMutableMap()
        var hasChanges = false
        
        currentStates.entries.removeAll { (_, state) ->
            if (state.isActive && state.snoozeTime <= currentTime) {
                hasChanges = true
                true
            } else {
                false
            }
        }
        
        if (hasChanges) {
            _snoozeStates.value = currentStates
            saveSnoozeStates()
        }
    }
}

/**
 * 贪睡状态数据类
 */
data class SnoozeState(
    val alarmId: Long,
    val snoozeTime: Long,
    val snoozeCount: Int,
    val originalTriggerTime: Long,
    val isActive: Boolean
)

/**
 * 贪睡记录数据类
 */
data class SnoozeRecord(
    val alarmId: Long,
    val snoozeTime: Long,
    val snoozeDuration: Int,
    val timestamp: Long
)

/**
 * 贪睡统计数据类
 */
data class SnoozeStats(
    val currentSnoozeCount: Int,
    val totalSnoozeCount: Int,
    val averageSnoozeDuration: Int,
    val lastSnoozeTime: Long?,
    val isCurrentlySnoozed: Boolean
)

/**
 * 贪睡结果密封类
 */
sealed class SnoozeResult {
    data class Success(val snoozeTime: Long, val snoozeCount: Int) : SnoozeResult()
    data class Error(val message: String) : SnoozeResult()
}