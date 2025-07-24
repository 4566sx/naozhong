package com.biblealarm.app.ui.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biblealarm.app.data.model.Alarm
import com.biblealarm.app.data.repository.AlarmRepository
import com.biblealarm.app.manager.AlarmManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 闹钟管理页面ViewModel
 */
@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmManager: AlarmManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmsUiState())
    val uiState: StateFlow<AlarmsUiState> = _uiState.asStateFlow()

    init {
        loadAlarms()
    }

    /**
     * 加载所有闹钟
     */
    fun loadAlarms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                alarmRepository.getAllAlarms()
                    .catch { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "加载闹钟失败: ${exception.message}"
                            )
                        }
                    }
                    .collect { alarms ->
                        val sortedAlarms = alarms.sortedWith(
                            compareBy<Alarm> { !it.isEnabled }
                                .thenBy { it.hour }
                                .thenBy { it.minute }
                        )
                        
                        val nextAlarm = findNextAlarm(alarms.filter { it.isEnabled })
                        
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                alarms = sortedAlarms,
                                nextAlarm = nextAlarm,
                                errorMessage = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载闹钟失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 切换闹钟启用状态
     */
    fun toggleAlarm(alarmId: Long, enabled: Boolean) {
        viewModelScope.launch {
            try {
                val alarm = alarmRepository.getAlarmById(alarmId)
                if (alarm != null) {
                    val updatedAlarm = alarm.copy(isEnabled = enabled)
                    alarmRepository.updateAlarm(updatedAlarm)
                    
                    if (enabled) {
                        alarmManager.scheduleAlarm(updatedAlarm)
                    } else {
                        alarmManager.cancelAlarm(updatedAlarm)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "更新闹钟失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 删除闹钟
     */
    fun deleteAlarm(alarmId: Long) {
        viewModelScope.launch {
            try {
                val alarm = alarmRepository.getAlarmById(alarmId)
                if (alarm != null) {
                    // 先取消系统闹钟
                    alarmManager.cancelAlarm(alarm)
                    // 再删除数据库记录
                    alarmRepository.deleteAlarm(alarm)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "删除闹钟失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 批量操作：启用所有闹钟
     */
    fun enableAllAlarms() {
        viewModelScope.launch {
            try {
                val alarms = _uiState.value.alarms
                alarms.forEach { alarm ->
                    if (!alarm.isEnabled) {
                        val updatedAlarm = alarm.copy(isEnabled = true)
                        alarmRepository.updateAlarm(updatedAlarm)
                        alarmManager.scheduleAlarm(updatedAlarm)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "批量启用失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 批量操作：禁用所有闹钟
     */
    fun disableAllAlarms() {
        viewModelScope.launch {
            try {
                val alarms = _uiState.value.alarms
                alarms.forEach { alarm ->
                    if (alarm.isEnabled) {
                        val updatedAlarm = alarm.copy(isEnabled = false)
                        alarmRepository.updateAlarm(updatedAlarm)
                        alarmManager.cancelAlarm(updatedAlarm)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "批量禁用失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 批量操作：删除所有闹钟
     */
    fun deleteAllAlarms() {
        viewModelScope.launch {
            try {
                val alarms = _uiState.value.alarms
                alarms.forEach { alarm ->
                    alarmManager.cancelAlarm(alarm)
                    alarmRepository.deleteAlarm(alarm)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "批量删除失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 复制闹钟
     */
    fun duplicateAlarm(alarmId: Long) {
        viewModelScope.launch {
            try {
                val originalAlarm = alarmRepository.getAlarmById(alarmId)
                if (originalAlarm != null) {
                    val duplicatedAlarm = originalAlarm.copy(
                        id = 0, // 新ID将由数据库自动生成
                        label = "${originalAlarm.label} (副本)",
                        isEnabled = false // 默认禁用副本
                    )
                    alarmRepository.insertAlarm(duplicatedAlarm)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "复制闹钟失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 查找下一个要响的闹钟
     */
    private fun findNextAlarm(enabledAlarms: List<Alarm>): Alarm? {
        if (enabledAlarms.isEmpty()) return null

        val now = LocalDateTime.now()
        val currentDayOfWeek = now.dayOfWeek.value % 7 // 转换为0-6，周日为0

        // 查找今天剩余时间内的闹钟
        val todayAlarms = enabledAlarms.filter { alarm ->
            alarm.repeatDays.isEmpty() || alarm.repeatDays.contains(currentDayOfWeek)
        }.filter { alarm ->
            val alarmTime = now.withHour(alarm.hour).withMinute(alarm.minute).withSecond(0)
            alarmTime.isAfter(now)
        }.minByOrNull { alarm ->
            val alarmTime = now.withHour(alarm.hour).withMinute(alarm.minute).withSecond(0)
            alarmTime
        }

        if (todayAlarms != null) {
            return todayAlarms
        }

        // 查找未来7天内的下一个闹钟
        for (dayOffset in 1..7) {
            val targetDate = now.plusDays(dayOffset.toLong())
            val targetDayOfWeek = targetDate.dayOfWeek.value % 7

            val dayAlarms = enabledAlarms.filter { alarm ->
                alarm.repeatDays.isEmpty() || alarm.repeatDays.contains(targetDayOfWeek)
            }.minByOrNull { alarm ->
                alarm.hour * 60 + alarm.minute
            }

            if (dayAlarms != null) {
                return dayAlarms
            }
        }

        return null
    }

    /**
     * 获取闹钟统计信息
     */
    fun getAlarmStats(): AlarmStats {
        val alarms = _uiState.value.alarms
        return AlarmStats(
            totalCount = alarms.size,
            enabledCount = alarms.count { it.isEnabled },
            disabledCount = alarms.count { !it.isEnabled },
            weekdayCount = alarms.count { alarm ->
                alarm.repeatDays.isNotEmpty() && 
                alarm.repeatDays.any { it in 1..5 } // 周一到周五
            },
            weekendCount = alarms.count { alarm ->
                alarm.repeatDays.isNotEmpty() && 
                alarm.repeatDays.any { it == 0 || it == 6 } // 周六周日
            },
            onceCount = alarms.count { it.repeatDays.isEmpty() }
        )
    }
}

/**
 * 闹钟管理页面UI状态
 */
data class AlarmsUiState(
    val isLoading: Boolean = false,
    val alarms: List<Alarm> = emptyList(),
    val nextAlarm: Alarm? = null,
    val errorMessage: String? = null
)

/**
 * 闹钟统计信息
 */
data class AlarmStats(
    val totalCount: Int = 0,
    val enabledCount: Int = 0,
    val disabledCount: Int = 0,
    val weekdayCount: Int = 0,
    val weekendCount: Int = 0,
    val onceCount: Int = 0
)