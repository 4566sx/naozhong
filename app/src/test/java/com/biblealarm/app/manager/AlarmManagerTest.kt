package com.biblealarm.app.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.biblealarm.app.data.model.Alarm
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 闹钟管理器测试
 */
class AlarmManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSystemAlarmManager: AlarmManager

    @Mock
    private lateinit var mockPendingIntent: PendingIntent

    private lateinit var alarmManager: com.biblealarm.app.manager.AlarmManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockSystemAlarmManager)
        alarmManager = com.biblealarm.app.manager.AlarmManager(mockContext)
    }

    @Test
    fun `测试调度单次闹钟`() = runTest {
        // 准备测试数据
        val alarm = Alarm(
            id = 1,
            hour = 8,
            minute = 30,
            label = "测试闹钟",
            isEnabled = true,
            repeatDays = emptyList(),
            isSnoozeEnabled = true,
            snoozeDuration = 10,
            isVibrateEnabled = true,
            volume = 0.8f
        )

        // 执行测试
        alarmManager.scheduleAlarm(alarm)

        // 验证系统闹钟管理器被调用
        verify(mockSystemAlarmManager, atLeastOnce()).setExactAndAllowWhileIdle(
            eq(AlarmManager.RTC_WAKEUP),
            any(),
            any()
        )
    }

    @Test
    fun `测试调度重复闹钟`() = runTest {
        // 准备测试数据 - 工作日闹钟
        val alarm = Alarm(
            id = 2,
            hour = 7,
            minute = 0,
            label = "工作日闹钟",
            isEnabled = true,
            repeatDays = listOf(1, 2, 3, 4, 5), // 周一到周五
            isSnoozeEnabled = true,
            snoozeDuration = 5,
            isVibrateEnabled = true,
            volume = 0.7f
        )

        // 执行测试
        alarmManager.scheduleAlarm(alarm)

        // 验证为每个重复日都设置了闹钟
        verify(mockSystemAlarmManager, times(5)).setExactAndAllowWhileIdle(
            eq(AlarmManager.RTC_WAKEUP),
            any(),
            any()
        )
    }

    @Test
    fun `测试取消闹钟`() = runTest {
        // 准备测试数据
        val alarm = Alarm(
            id = 3,
            hour = 9,
            minute = 15,
            label = "要取消的闹钟",
            isEnabled = false,
            repeatDays = emptyList(),
            isSnoozeEnabled = false,
            snoozeDuration = 10,
            isVibrateEnabled = false,
            volume = 0.5f
        )

        // 执行测试
        alarmManager.cancelAlarm(alarm)

        // 验证系统闹钟管理器的取消方法被调用
        verify(mockSystemAlarmManager, atLeastOnce()).cancel(any<PendingIntent>())
    }

    @Test
    fun `测试计算下次闹钟时间 - 今天还未到时间`() {
        val now = LocalDateTime.of(2024, 1, 15, 8, 0) // 周一 8:00
        val alarmTime = LocalTime.of(9, 30) // 9:30
        
        val nextTime = alarmManager.calculateNextAlarmTime(now, alarmTime, emptyList())
        
        // 应该是今天的9:30
        assertEquals(2024, nextTime.year)
        assertEquals(1, nextTime.monthValue)
        assertEquals(15, nextTime.dayOfMonth)
        assertEquals(9, nextTime.hour)
        assertEquals(30, nextTime.minute)
    }

    @Test
    fun `测试计算下次闹钟时间 - 今天已过时间`() {
        val now = LocalDateTime.of(2024, 1, 15, 10, 0) // 周一 10:00
        val alarmTime = LocalTime.of(9, 30) // 9:30
        
        val nextTime = alarmManager.calculateNextAlarmTime(now, alarmTime, emptyList())
        
        // 应该是明天的9:30
        assertEquals(2024, nextTime.year)
        assertEquals(1, nextTime.monthValue)
        assertEquals(16, nextTime.dayOfMonth)
        assertEquals(9, nextTime.hour)
        assertEquals(30, nextTime.minute)
    }

    @Test
    fun `测试计算下次闹钟时间 - 工作日重复`() {
        val now = LocalDateTime.of(2024, 1, 15, 10, 0) // 周一 10:00
        val alarmTime = LocalTime.of(9, 30) // 9:30
        val repeatDays = listOf(1, 2, 3, 4, 5) // 周一到周五
        
        val nextTime = alarmManager.calculateNextAlarmTime(now, alarmTime, repeatDays)
        
        // 应该是明天（周二）的9:30
        assertEquals(2024, nextTime.year)
        assertEquals(1, nextTime.monthValue)
        assertEquals(16, nextTime.dayOfMonth)
        assertEquals(9, nextTime.hour)
        assertEquals(30, nextTime.minute)
    }

    @Test
    fun `测试计算下次闹钟时间 - 周末重复跨周`() {
        val now = LocalDateTime.of(2024, 1, 14, 10, 0) // 周日 10:00
        val alarmTime = LocalTime.of(9, 30) // 9:30
        val repeatDays = listOf(0, 6) // 周六和周日
        
        val nextTime = alarmManager.calculateNextAlarmTime(now, alarmTime, repeatDays)
        
        // 应该是下周六的9:30
        assertEquals(2024, nextTime.year)
        assertEquals(1, nextTime.monthValue)
        assertEquals(20, nextTime.dayOfMonth) // 下周六
        assertEquals(9, nextTime.hour)
        assertEquals(30, nextTime.minute)
    }

    @Test
    fun `测试闹钟是否应该在指定日期触发`() {
        val alarm = Alarm(
            id = 4,
            hour = 8,
            minute = 0,
            label = "测试闹钟",
            isEnabled = true,
            repeatDays = listOf(1, 3, 5), // 周一、周三、周五
            isSnoozeEnabled = true,
            snoozeDuration = 10,
            isVibrateEnabled = true,
            volume = 0.8f
        )

        val monday = LocalDateTime.of(2024, 1, 15, 8, 0) // 周一
        val tuesday = LocalDateTime.of(2024, 1, 16, 8, 0) // 周二
        val wednesday = LocalDateTime.of(2024, 1, 17, 8, 0) // 周三

        assertTrue("周一应该触发", alarmManager.shouldTriggerOnDate(alarm, monday))
        assertFalse("周二不应该触发", alarmManager.shouldTriggerOnDate(alarm, tuesday))
        assertTrue("周三应该触发", alarmManager.shouldTriggerOnDate(alarm, wednesday))
    }

    @Test
    fun `测试禁用的闹钟不会被调度`() = runTest {
        val disabledAlarm = Alarm(
            id = 5,
            hour = 8,
            minute = 0,
            label = "禁用的闹钟",
            isEnabled = false,
            repeatDays = emptyList(),
            isSnoozeEnabled = true,
            snoozeDuration = 10,
            isVibrateEnabled = true,
            volume = 0.8f
        )

        alarmManager.scheduleAlarm(disabledAlarm)

        // 验证系统闹钟管理器没有被调用
        verify(mockSystemAlarmManager, never()).setExactAndAllowWhileIdle(any(), any(), any())
    }

    @Test
    fun `测试贪睡功能调度`() = runTest {
        val alarm = Alarm(
            id = 6,
            hour = 8,
            minute = 0,
            label = "贪睡测试",
            isEnabled = true,
            repeatDays = emptyList(),
            isSnoozeEnabled = true,
            snoozeDuration = 10,
            isVibrateEnabled = true,
            volume = 0.8f
        )

        alarmManager.scheduleSnooze(alarm)

        // 验证贪睡闹钟被设置（应该在10分钟后）
        verify(mockSystemAlarmManager).setExactAndAllowWhileIdle(
            eq(AlarmManager.RTC_WAKEUP),
            any(),
            any()
        )
    }

    @Test
    fun `测试批量操作 - 调度多个闹钟`() = runTest {
        val alarms = listOf(
            Alarm(id = 7, hour = 7, minute = 0, label = "闹钟1", isEnabled = true),
            Alarm(id = 8, hour = 8, minute = 30, label = "闹钟2", isEnabled = true),
            Alarm(id = 9, hour = 9, minute = 15, label = "闹钟3", isEnabled = false)
        )

        alarms.forEach { alarm ->
            alarmManager.scheduleAlarm(alarm)
        }

        // 验证只有启用的闹钟被调度（2个）
        verify(mockSystemAlarmManager, times(2)).setExactAndAllowWhileIdle(
            eq(AlarmManager.RTC_WAKEUP),
            any(),
            any()
        )
    }
}