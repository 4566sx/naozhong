package com.biblealarm.app.manager

import android.content.Context
import android.content.SharedPreferences
import com.biblealarm.app.data.model.Psalm
import com.biblealarm.app.data.repository.AudioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * 诗篇选择管理器
 * 负责每日随机选择诗篇的逻辑
 */
@Singleton
class PsalmSelectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioRepository: AudioRepository
) {
    
    companion object {
        private const val PREFS_NAME = "psalm_selection_prefs"
        private const val KEY_LAST_SELECTION_DATE = "last_selection_date"
        private const val KEY_TODAY_PSALM_NUMBER = "today_psalm_number"
        private const val KEY_SELECTION_HISTORY = "selection_history"
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    
    /**
     * 获取今日诗篇
     * 如果今天还没有选择，则随机选择一篇
     */
    suspend fun getTodayPsalm(): Psalm? {
        val today = dateFormat.format(Date())
        val lastSelectionDate = prefs.getString(KEY_LAST_SELECTION_DATE, "")
        
        return if (today == lastSelectionDate) {
            // 今天已经选择过，返回已选择的诗篇
            val todayPsalmNumber = prefs.getInt(KEY_TODAY_PSALM_NUMBER, -1)
            if (todayPsalmNumber != -1) {
                audioRepository.getPsalmByNumber(todayPsalmNumber)
            } else {
                selectTodayPsalm()
            }
        } else {
            // 今天还没有选择，进行新的选择
            selectTodayPsalm()
        }
    }
    
    /**
     * 选择今日诗篇
     */
    private suspend fun selectTodayPsalm(): Psalm? {
        val availablePsalms = audioRepository.getAvailablePsalms().first()
        
        if (availablePsalms.isEmpty()) {
            return null
        }
        
        val selectedPsalm = when (getSelectionStrategy()) {
            SelectionStrategy.PURE_RANDOM -> selectPureRandom(availablePsalms)
            SelectionStrategy.WEIGHTED_RANDOM -> selectWeightedRandom(availablePsalms)
            SelectionStrategy.SEQUENTIAL -> selectSequential(availablePsalms)
            SelectionStrategy.AVOID_RECENT -> selectAvoidingRecent(availablePsalms)
        }
        
        selectedPsalm?.let { psalm ->
            saveTodaySelection(psalm.number)
            updateSelectionHistory(psalm.number)
        }
        
        return selectedPsalm
    }
    
    /**
     * 纯随机选择
     */
    private fun selectPureRandom(availablePsalms: List<Psalm>): Psalm {
        return availablePsalms.random()
    }
    
    /**
     * 加权随机选择（较少使用的诗篇有更高概率被选中）
     */
    private fun selectWeightedRandom(availablePsalms: List<Psalm>): Psalm {
        // 计算权重：使用次数越少，权重越高
        val maxUsage = availablePsalms.maxOfOrNull { it.usageCount } ?: 0
        val weights = availablePsalms.map { psalm ->
            // 权重 = (最大使用次数 - 当前使用次数 + 1)
            (maxUsage - psalm.usageCount + 1).toDouble()
        }
        
        val totalWeight = weights.sum()
        val randomValue = Random.nextDouble() * totalWeight
        
        var currentWeight = 0.0
        for (i in availablePsalms.indices) {
            currentWeight += weights[i]
            if (randomValue <= currentWeight) {
                return availablePsalms[i]
            }
        }
        
        // 备用方案
        return availablePsalms.random()
    }
    
    /**
     * 顺序选择（按编号顺序循环）
     */
    private fun selectSequential(availablePsalms: List<Psalm>): Psalm {
        val lastPsalmNumber = prefs.getInt(KEY_TODAY_PSALM_NUMBER, 0)
        val sortedPsalms = availablePsalms.sortedBy { it.number }
        
        val currentIndex = sortedPsalms.indexOfFirst { it.number > lastPsalmNumber }
        return if (currentIndex != -1) {
            sortedPsalms[currentIndex]
        } else {
            sortedPsalms.first() // 循环到第一篇
        }
    }
    
    /**
     * 避免最近选择的诗篇
     */
    private fun selectAvoidingRecent(availablePsalms: List<Psalm>): Psalm {
        val recentSelections = getRecentSelections(7) // 最近7天的选择
        val availableForSelection = availablePsalms.filter { psalm ->
            !recentSelections.contains(psalm.number)
        }
        
        return if (availableForSelection.isNotEmpty()) {
            selectWeightedRandom(availableForSelection)
        } else {
            // 如果所有诗篇都在最近选择过，则使用加权随机
            selectWeightedRandom(availablePsalms)
        }
    }
    
    /**
     * 保存今日选择
     */
    private fun saveTodaySelection(psalmNumber: Int) {
        val today = dateFormat.format(Date())
        prefs.edit()
            .putString(KEY_LAST_SELECTION_DATE, today)
            .putInt(KEY_TODAY_PSALM_NUMBER, psalmNumber)
            .apply()
    }
    
    /**
     * 更新选择历史
     */
    private fun updateSelectionHistory(psalmNumber: Int) {
        val today = dateFormat.format(Date())
        val historyJson = prefs.getString(KEY_SELECTION_HISTORY, "{}") ?: "{}"
        
        try {
            val history = parseSelectionHistory(historyJson).toMutableMap()
            history[today] = psalmNumber
            
            // 只保留最近30天的历史
            val thirtyDaysAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30)
            }.time
            val cutoffDate = dateFormat.format(thirtyDaysAgo)
            
            val filteredHistory = history.filter { (date, _) ->
                date >= cutoffDate
            }
            
            val updatedHistoryJson = formatSelectionHistory(filteredHistory)
            prefs.edit()
                .putString(KEY_SELECTION_HISTORY, updatedHistoryJson)
                .apply()
                
        } catch (e: Exception) {
            // 如果解析失败，重新开始记录
            val newHistory = mapOf(today to psalmNumber)
            val newHistoryJson = formatSelectionHistory(newHistory)
            prefs.edit()
                .putString(KEY_SELECTION_HISTORY, newHistoryJson)
                .apply()
        }
    }
    
    /**
     * 获取最近的选择记录
     */
    private fun getRecentSelections(days: Int): List<Int> {
        val historyJson = prefs.getString(KEY_SELECTION_HISTORY, "{}") ?: "{}"
        val history = parseSelectionHistory(historyJson)
        
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.time
        val cutoffDateStr = dateFormat.format(cutoffDate)
        
        return history.filter { (date, _) ->
            date >= cutoffDateStr
        }.values.toList()
    }
    
    /**
     * 解析选择历史JSON
     */
    private fun parseSelectionHistory(json: String): Map<String, Int> {
        return try {
            val pairs = json.removeSurrounding("{", "}")
                .split(",")
                .mapNotNull { pair ->
                    val parts = pair.split(":")
                    if (parts.size == 2) {
                        val date = parts[0].trim().removeSurrounding("\"")
                        val number = parts[1].trim().toIntOrNull()
                        if (number != null) date to number else null
                    } else null
                }
            pairs.toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * 格式化选择历史为JSON
     */
    private fun formatSelectionHistory(history: Map<String, Int>): String {
        val pairs = history.map { (date, number) ->
            "\"$date\":$number"
        }
        return "{${pairs.joinToString(",")}}"
    }
    
    /**
     * 获取选择策略
     */
    private fun getSelectionStrategy(): SelectionStrategy {
        val strategyName = prefs.getString("selection_strategy", SelectionStrategy.WEIGHTED_RANDOM.name)
        return try {
            SelectionStrategy.valueOf(strategyName ?: SelectionStrategy.WEIGHTED_RANDOM.name)
        } catch (e: Exception) {
            SelectionStrategy.WEIGHTED_RANDOM
        }
    }
    
    /**
     * 设置选择策略
     */
    fun setSelectionStrategy(strategy: SelectionStrategy) {
        prefs.edit()
            .putString("selection_strategy", strategy.name)
            .apply()
    }
    
    /**
     * 手动重新选择今日诗篇
     */
    suspend fun reselectTodayPsalm(): Psalm? {
        // 清除今日选择记录
        prefs.edit()
            .remove(KEY_LAST_SELECTION_DATE)
            .remove(KEY_TODAY_PSALM_NUMBER)
            .apply()
        
        return selectTodayPsalm()
    }
    
    /**
     * 获取选择历史统计
     */
    fun getSelectionStats(): SelectionStats {
        val historyJson = prefs.getString(KEY_SELECTION_HISTORY, "{}") ?: "{}"
        val history = parseSelectionHistory(historyJson)
        
        val totalSelections = history.size
        val uniquePsalms = history.values.toSet().size
        val mostSelected = history.values.groupingBy { it }.eachCount().maxByOrNull { it.value }
        
        return SelectionStats(
            totalSelections = totalSelections,
            uniquePsalms = uniquePsalms,
            mostSelectedPsalm = mostSelected?.key,
            mostSelectedCount = mostSelected?.value ?: 0,
            selectionHistory = history
        )
    }
    
    /**
     * 清除选择历史
     */
    fun clearSelectionHistory() {
        prefs.edit()
            .remove(KEY_SELECTION_HISTORY)
            .remove(KEY_LAST_SELECTION_DATE)
            .remove(KEY_TODAY_PSALM_NUMBER)
            .apply()
    }
}

/**
 * 选择策略枚举
 */
enum class SelectionStrategy {
    PURE_RANDOM,        // 纯随机
    WEIGHTED_RANDOM,    // 加权随机（推荐）
    SEQUENTIAL,         // 顺序选择
    AVOID_RECENT        // 避免最近选择
}

/**
 * 选择统计数据类
 */
data class SelectionStats(
    val totalSelections: Int,           // 总选择次数
    val uniquePsalms: Int,              // 不重复诗篇数
    val mostSelectedPsalm: Int?,        // 最常选择的诗篇
    val mostSelectedCount: Int,         // 最常选择的次数
    val selectionHistory: Map<String, Int> // 选择历史
)