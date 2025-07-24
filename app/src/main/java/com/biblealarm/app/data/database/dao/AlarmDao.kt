package com.biblealarm.app.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.biblealarm.app.data.model.Alarm

/**
 * 闹钟数据访问对象
 */
@Dao
interface AlarmDao {
    
    /**
     * 获取所有闹钟
     */
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<Alarm>>
    
    /**
     * 获取启用的闹钟
     */
    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour, minute")
    fun getEnabledAlarms(): Flow<List<Alarm>>
    
    /**
     * 根据ID获取闹钟
     */
    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): Alarm?
    
    /**
     * 插入闹钟
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long
    
    /**
     * 插入多个闹钟
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<Alarm>)
    
    /**
     * 更新闹钟
     */
    @Update
    suspend fun updateAlarm(alarm: Alarm)
    
    /**
     * 删除闹钟
     */
    @Delete
    suspend fun deleteAlarm(alarm: Alarm)
    
    /**
     * 根据ID删除闹钟
     */
    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)
    
    /**
     * 启用/禁用闹钟
     */
    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean)
    
    /**
     * 更新闹钟最后触发时间
     */
    @Query("UPDATE alarms SET lastTriggered = :timestamp WHERE id = :id")
    suspend fun updateLastTriggered(id: Long, timestamp: Long)
    
    /**
     * 获取下一个要触发的闹钟
     */
    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour, minute LIMIT 1")
    suspend fun getNextAlarm(): Alarm?
    
    /**
     * 清空所有闹钟
     */
    @Query("DELETE FROM alarms")
    suspend fun deleteAllAlarms()
    
    /**
     * 获取闹钟总数
     */
    @Query("SELECT COUNT(*) FROM alarms")
    suspend fun getAlarmCount(): Int
    
    /**
     * 获取启用的闹钟总数
     */
    @Query("SELECT COUNT(*) FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarmCount(): Int
}