package com.biblealarm.app.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.biblealarm.app.data.model.Psalm

/**
 * 诗篇数据访问对象
 */
@Dao
interface PsalmDao {
    
    /**
     * 获取所有诗篇
     */
    @Query("SELECT * FROM psalms ORDER BY number")
    fun getAllPsalms(): Flow<List<Psalm>>
    
    /**
     * 获取可用的诗篇
     */
    @Query("SELECT * FROM psalms WHERE isAvailable = 1 ORDER BY number")
    fun getAvailablePsalms(): Flow<List<Psalm>>
    
    /**
     * 根据编号获取诗篇
     */
    @Query("SELECT * FROM psalms WHERE number = :number")
    suspend fun getPsalmByNumber(number: Int): Psalm?
    
    /**
     * 获取随机诗篇
     */
    @Query("SELECT * FROM psalms WHERE isAvailable = 1 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPsalm(): Psalm?
    
    /**
     * 获取指定数量的随机诗篇
     */
    @Query("SELECT * FROM psalms WHERE isAvailable = 1 ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomPsalms(count: Int): List<Psalm>
    
    /**
     * 插入诗篇
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPsalm(psalm: Psalm)
    
    /**
     * 插入多个诗篇
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPsalms(psalms: List<Psalm>)
    
    /**
     * 更新诗篇
     */
    @Update
    suspend fun updatePsalm(psalm: Psalm)
    
    /**
     * 删除诗篇
     */
    @Delete
    suspend fun deletePsalm(psalm: Psalm)
    
    /**
     * 更新诗篇可用状态
     */
    @Query("UPDATE psalms SET isAvailable = :available WHERE number = :number")
    suspend fun updatePsalmAvailability(number: Int, available: Boolean)
    
    /**
     * 更新诗篇音频路径
     */
    @Query("UPDATE psalms SET audioFilePath = :path, isAvailable = :available WHERE number = :number")
    suspend fun updatePsalmAudioPath(number: Int, path: String, available: Boolean)
    
    /**
     * 更新诗篇使用记录
     */
    @Query("UPDATE psalms SET lastUsedDate = :date, usageCount = usageCount + 1 WHERE number = :number")
    suspend fun updatePsalmUsage(number: Int, date: String)
    
    /**
     * 获取最常使用的诗篇
     */
    @Query("SELECT * FROM psalms WHERE isAvailable = 1 ORDER BY usageCount DESC LIMIT :count")
    suspend fun getMostUsedPsalms(count: Int): List<Psalm>
    
    /**
     * 获取最近使用的诗篇
     */
    @Query("SELECT * FROM psalms WHERE isAvailable = 1 AND lastUsedDate IS NOT NULL ORDER BY lastUsedDate DESC LIMIT :count")
    suspend fun getRecentlyUsedPsalms(count: Int): List<Psalm>
    
    /**
     * 获取可用诗篇总数
     */
    @Query("SELECT COUNT(*) FROM psalms WHERE isAvailable = 1")
    suspend fun getAvailablePsalmCount(): Int
    
    /**
     * 获取诗篇总数
     */
    @Query("SELECT COUNT(*) FROM psalms")
    suspend fun getTotalPsalmCount(): Int
    
    /**
     * 清空所有诗篇
     */
    @Query("DELETE FROM psalms")
    suspend fun deleteAllPsalms()
    
    /**
     * 重置使用统计
     */
    @Query("UPDATE psalms SET usageCount = 0, lastUsedDate = NULL")
    suspend fun resetUsageStats()
    
    /**
     * 根据编号范围获取诗篇
     */
    @Query("SELECT * FROM psalms WHERE number BETWEEN :start AND :end ORDER BY number")
    suspend fun getPsalmsByRange(start: Int, end: Int): List<Psalm>
    
    /**
     * 搜索诗篇标题
     */
    @Query("SELECT * FROM psalms WHERE title LIKE '%' || :query || '%' ORDER BY number")
    suspend fun searchPsalmsByTitle(query: String): List<Psalm>
    
    /**
     * 获取所有诗篇（同步方法，用于内部处理）
     */
    @Query("SELECT * FROM psalms ORDER BY number")
    suspend fun getAllPsalmsSync(): List<Psalm>
    
    /**
     * 获取用户音频文件数量（非内置音频）
     */
    @Query("SELECT COUNT(*) FROM psalms WHERE isAvailable = 1 AND audioFilePath NOT LIKE '%assets%'")
    suspend fun getUserAudioCount(): Int
    
    /**
     * 获取可用音频文件总数
     */
    @Query("SELECT COUNT(*) FROM psalms WHERE isAvailable = 1")
    suspend fun getAvailableAudioCount(): Int
    
    /**
     * 获取内置音频文件数量
     */
    @Query("SELECT COUNT(*) FROM psalms WHERE isAvailable = 1 AND audioFilePath LIKE '%assets%'")
    suspend fun getBuiltInAudioCount(): Int
    
    /**
     * 更新诗篇为内置音频
     */
    @Query("UPDATE psalms SET audioFilePath = :path, isAvailable = 1 WHERE number = :number")
    suspend fun updatePsalmToBuiltIn(number: Int, path: String)
}
