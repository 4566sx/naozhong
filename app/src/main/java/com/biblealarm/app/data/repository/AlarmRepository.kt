package com.biblealarm.app.data.repository

import com.biblealarm.app.data.database.dao.AlarmDao
import com.biblealarm.app.data.model.Alarm
import com.biblealarm.app.manager.AlarmManager
import com.biblealarm.app.manager.AlarmSetResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 闹钟数据仓库
 */
@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmManager: AlarmManager
) {
    
    /**
     * 获取所有闹钟
     */
    fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()
    
    /**
     * 获取启用的闹钟
     */
    fun getEnabledAlarms(): Flow<List<Alarm>> = alarmDao.getEnabledAlarms()
    
    /**
     * 根据ID获取闹钟
     */
    suspend fun getAlarmById(id: Long): Alarm? = alarmDao.getAlarmById(id)
    
    /**
     * 添加闹钟
     */
    suspend fun addAlarm(alarm: Alarm): AlarmSetResult {
        return try {
            // 检查权限
            if (!alarmManager.canScheduleExactAlarms()) {
                return AlarmSetResult.PermissionRequired
            }
            
            // 插入数据库
            val alarmId = alarmDao.insertAlarm(alarm)
            val savedAlarm = alarm.copy(id = alarmId)
            
            // 设置系统闹钟
            if (savedAlarm.isEnabled) {
                alarmManager.setAlarm(savedAlarm)
            }
            
            AlarmSetResult.Success
        } catch (e: Exception) {
            AlarmSetResult.Error("添加闹钟失败: ${e.message}")
        }
    }
    
    /**
     * 更新闹钟
     */
    suspend fun updateAlarm(alarm: Alarm): AlarmSetResult {
        return try {
            // 检查权限
            if (alarm.isEnabled && !alarmManager.canScheduleExactAlarms()) {
                return AlarmSetResult.PermissionRequired
            }
            
            // 更新数据库
            alarmDao.updateAlarm(alarm)
            
            // 重新设置系统闹钟
            if (alarm.isEnabled) {
                alarmManager.setAlarm(alarm)
            } else {
                alarmManager.cancelAlarm(alarm)
            }
            
            AlarmSetResult.Success
        } catch (e: Exception) {
            AlarmSetResult.Error("更新闹钟失败: ${e.message}")
        }
    }
    
    /**
     * 删除闹钟
     */
    suspend fun deleteAlarm(alarm: Alarm): Boolean {
        return try {
            // 取消系统闹钟
            alarmManager.cancelAlarm(alarm)
            
            // 从数据库删除
            alarmDao.deleteAlarm(alarm)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 根据ID删除闹钟
     */
    suspend fun deleteAlarmById(id: Long): Boolean {
        return try {
            val alarm = alarmDao.getAlarmById(id)
            if (alarm != null) {
                deleteAlarm(alarm)
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 启用/禁用闹钟
     */
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean): AlarmSetResult {
        return try {
            val alarm = alarmDao.getAlarmById(id)
            if (alarm == null) {
                return AlarmSetResult.Error("找不到指定的闹钟")
            }
            
            // 检查权限
            if (enabled && !alarmManager.canScheduleExactAlarms()) {
                return AlarmSetResult.PermissionRequired
            }
            
            // 更新数据库
            alarmDao.setAlarmEnabled(id, enabled)
            
            // 更新系统闹钟
            val updatedAlarm = alarm.copy(isEnabled = enabled)
            if (enabled) {
                alarmManager.setAlarm(updatedAlarm)
            } else {
                alarmManager.cancelAlarm(updatedAlarm)
            }
            
            AlarmSetResult.Success
        } catch (e: Exception) {
            AlarmSetResult.Error("设置闹钟状态失败: ${e.message}")
        }
    }
    
    /**
     * 获取下一个要触发的闹钟
     */
    suspend fun getNextAlarm(): Alarm? {
        return try {
            val enabledAlarms = alarmDao.getEnabledAlarms().first()
            
            // 找到最近要触发的闹钟
            enabledAlarms.minByOrNull { alarm ->
                alarmManager.getNextAlarmTime(alarm) ?: Long.MAX_VALUE
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取闹钟统计信息
     */
    suspend fun getAlarmStats(): AlarmStats {
        return try {
            val totalCount = alarmDao.getAlarmCount()
            val enabledCount = alarmDao.getEnabledAlarmCount()
            val nextAlarm = getNextAlarm()
            val nextTriggerTime = nextAlarm?.let { alarmManager.getNextAlarmTime(it) }
            
            AlarmStats(
                totalCount = totalCount,
                enabledCount = enabledCount,
                nextAlarm = nextAlarm,
                nextTriggerTime = nextTriggerTime
            )
        } catch (e: Exception) {
            AlarmStats(0, 0, null, null)
        }
    }
    
    /**
     * 重新设置所有启用的闹钟
     */
    suspend fun rescheduleAllAlarms(): Boolean {
        return try {
            val enabledAlarms = alarmDao.getEnabledAlarms().first()
            
            enabledAlarms.forEach { alarm ->
                try {
                    alarmManager.setAlarm(alarm)
                } catch (e: Exception) {
                    // 记录错误但继续处理其他闹钟
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 验证闹钟设置
     */
    suspend fun validateAlarmSettings(): ValidationResult {
        return try {
            val alarms = alarmDao.getAllAlarms().first()
            val issues = mutableListOf<String>()
            
            // 检查权限
            if (!alarmManager.canScheduleExactAlarms()) {
                issues.add("缺少精确闹钟权限")
            }
            
            // 检查闹钟冲突
            val enabledAlarms = alarms.filter { it.isEnabled }
            val timeConflicts = findTimeConflicts(enabledAlarms)
            if (timeConflicts.isNotEmpty()) {
                issues.add("发现时间冲突的闹钟: ${timeConflicts.size}个")
            }
            
            // 检查无效设置
            val invalidAlarms = alarms.filter { alarm ->
                alarm.hour !in 0..23 || alarm.minute !in 0..59
            }
            if (invalidAlarms.isNotEmpty()) {
                issues.add("发现无效时间设置: ${invalidAlarms.size}个")
            }
            
            ValidationResult(
                isValid = issues.isEmpty(),
                issues = issues,
                totalAlarms = alarms.size,
                enabledAlarms = enabledAlarms.size
            )
        } catch (e: Exception) {
            ValidationResult(
                isValid = false,
                issues = listOf("验证失败: ${e.message}"),
                totalAlarms = 0,
                enabledAlarms = 0
            )
        }
    }
    
    /**
     * 查找时间冲突的闹钟
     */
    private fun findTimeConflicts(alarms: List<Alarm>): List<Pair<Alarm, Alarm>> {
        val conflicts = mutableListOf<Pair<Alarm, Alarm>>()
        
        for (i in alarms.indices) {
            for (j in i + 1 until alarms.size) {
                val alarm1 = alarms[i]
                val alarm2 = alarms[j]
                
                // 检查时间是否相同
                if (alarm1.hour == alarm2.hour && alarm1.minute == alarm2.minute) {
                    // 检查重复日期是否有重叠
                    val hasOverlap = when {
                        alarm1.repeatDays.isEmpty() && alarm2.repeatDays.isEmpty() -> true
                        alarm1.repeatDays.isEmpty() || alarm2.repeatDays.isEmpty() -> false
                        else -> alarm1.repeatDays.intersect(alarm2.repeatDays).isNotEmpty()
                    }
                    
                    if (hasOverlap) {
                        conflicts.add(alarm1 to alarm2)
                    }
                }
            }
        }
        
        return conflicts
    }
    
    /**
     * 清空所有闹钟
     */
    suspend fun clearAllAlarms(): Boolean {
        return try {
            // 取消所有系统闹钟
            val alarms = alarmDao.getAllAlarms().first()
            alarms.forEach { alarm ->
                try {
                    alarmManager.cancelAlarm(alarm)
                } catch (e: Exception) {
                    // 忽略取消失败的情况
                }
            }
            
            // 清空数据库
            alarmDao.deleteAllAlarms()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 导入预设闹钟
     */
    suspend fun importPresetAlarms(): Boolean {
        return try {
            val presetAlarms = Alarm.presetAlarms
            
            presetAlarms.forEach { alarm ->
                try {
                    addAlarm(alarm)
                } catch (e: Exception) {
                    // 忽略单个闹钟导入失败
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * 闹钟统计数据类
 */
data class AlarmStats(
    val totalCount: Int,
    val enabledCount: Int,
    val nextAlarm: Alarm?,
    val nextTriggerTime: Long?
)

/**
 * 验证结果数据类
 */
data class ValidationResult(
    val isValid: Boolean,
    val issues: List<String>,
    val totalAlarms: Int,
    val enabledAlarms: Int
)