package com.biblealarm.app

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.biblealarm.app.data.database.BibleAlarmDatabase
import com.biblealarm.app.data.model.Alarm
import com.biblealarm.app.data.model.Psalm
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * 圣经闹钟应用集成测试
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BibleAlarmIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: BibleAlarmDatabase

    private lateinit var context: Context

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // 初始化测试数据
        runBlocking {
            setupTestData()
        }
    }

    @Test
    fun 测试应用启动和主界面显示() {
        // 验证主界面元素是否正确显示
        composeTestRule.onNodeWithText("圣经闹钟").assertIsDisplayed()
        composeTestRule.onNodeWithText("今日诗篇").assertIsDisplayed()
        composeTestRule.onNodeWithText("音量控制").assertIsDisplayed()
        composeTestRule.onNodeWithText("下一个闹钟").assertIsDisplayed()
    }

    @Test
    fun 测试诗篇播放功能() {
        // 等待诗篇加载
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("诗篇").fetchSemanticsNodes().isNotEmpty()
        }

        // 点击播放按钮
        composeTestRule.onNodeWithContentDescription("播放").performClick()

        // 验证播放状态变化
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription("暂停").fetchSemanticsNodes().isNotEmpty()
        }

        // 点击暂停按钮
        composeTestRule.onNodeWithContentDescription("暂停").performClick()

        // 验证暂停状态
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithContentDescription("播放").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun 测试音量控制功能() {
        // 找到音量滑块
        composeTestRule.onNodeWithText("音量控制").assertIsDisplayed()
        
        // 测试静音按钮
        composeTestRule.onNodeWithContentDescription("静音").performClick()
        
        // 验证静音状态
        composeTestRule.onNodeWithText("静音").assertIsDisplayed()
        
        // 取消静音
        composeTestRule.onNodeWithContentDescription("取消静音").performClick()
    }

    @Test
    fun 测试导航到闹钟管理页面() {
        // 点击添加闹钟按钮
        composeTestRule.onNodeWithText("添加闹钟").performClick()

        // 验证导航到闹钟管理页面
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("闹钟管理").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun 测试导航到设置页面() {
        // 点击设置按钮
        composeTestRule.onNodeWithContentDescription("设置").performClick()

        // 验证导航到设置页面
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("应用设置").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun 测试闹钟创建流程() {
        // 导航到闹钟管理页面
        composeTestRule.onNodeWithText("添加闹钟").performClick()
        
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("闹钟管理").fetchSemanticsNodes().isNotEmpty()
        }

        // 点击添加闹钟按钮
        composeTestRule.onNodeWithContentDescription("添加闹钟").performClick()

        // 验证闹钟编辑界面
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("设置闹钟").fetchSemanticsNodes().isNotEmpty()
        }

        // 设置闹钟标签
        composeTestRule.onNodeWithText("闹钟标签").performTextInput("测试闹钟")

        // 保存闹钟
        composeTestRule.onNodeWithText("保存").performClick()

        // 验证返回到闹钟列表
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("测试闹钟").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun 测试权限检查功能() {
        // 导航到设置页面
        composeTestRule.onNodeWithContentDescription("设置").performClick()
        
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("应用设置").fetchSemanticsNodes().isNotEmpty()
        }

        // 查找权限管理部分
        composeTestRule.onNodeWithText("权限管理").assertIsDisplayed()
        
        // 验证权限状态显示
        composeTestRule.onNodeWithText("通知权限").assertIsDisplayed()
        composeTestRule.onNodeWithText("精确闹钟权限").assertIsDisplayed()
    }

    @Test
    fun 测试数据持久化() = runBlocking {
        // 创建测试闹钟
        val testAlarm = Alarm(
            id = 999,
            hour = 8,
            minute = 30,
            label = "集成测试闹钟",
            isEnabled = true,
            repeatDays = listOf(1, 2, 3, 4, 5),
            isSnoozeEnabled = true,
            snoozeDuration = 10,
            isVibrateEnabled = true,
            volume = 0.8f
        )

        // 保存到数据库
        database.alarmDao().insertAlarm(testAlarm)

        // 验证数据保存成功
        val savedAlarm = database.alarmDao().getAlarmById(999)
        assert(savedAlarm != null)
        assert(savedAlarm?.label == "集成测试闹钟")
        assert(savedAlarm?.hour == 8)
        assert(savedAlarm?.minute == 30)

        // 清理测试数据
        database.alarmDao().deleteAlarm(testAlarm)
    }

    @Test
    fun 测试诗篇数据完整性() = runBlocking {
        // 验证诗篇数据是否完整
        val allPsalms = database.psalmDao().getAllPsalms()
        
        assert(allPsalms.isNotEmpty()) { "诗篇数据不应为空" }
        assert(allPsalms.size <= 150) { "诗篇数量不应超过150" }
        
        // 验证诗篇编号的唯一性
        val psalmNumbers = allPsalms.map { it.number }.toSet()
        assert(psalmNumbers.size == allPsalms.size) { "诗篇编号应该是唯一的" }
        
        // 验证诗篇编号范围
        psalmNumbers.forEach { number ->
            assert(number in 1..150) { "诗篇编号应该在1-150范围内" }
        }
    }

    @Test
    fun 测试应用性能() {
        val startTime = System.currentTimeMillis()

        // 执行一系列操作
        composeTestRule.onNodeWithText("今日诗篇").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("播放").performClick()
        
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule.onAllNodesWithContentDescription("暂停").fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onNodeWithContentDescription("暂停").performClick()

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // 验证操作在合理时间内完成
        assert(duration < 5000) { "基本操作应该在5秒内完成" }
    }

    @Test
    fun 测试错误处理() {
        // 测试无效音频文件处理
        composeTestRule.onNodeWithContentDescription("播放").performClick()
        
        // 应用不应该崩溃，即使音频文件不存在
        composeTestRule.onNodeWithText("圣经闹钟").assertIsDisplayed()
    }

    @Test
    fun 测试界面响应性() {
        // 快速连续点击测试
        repeat(5) {
            composeTestRule.onNodeWithContentDescription("播放").performClick()
            Thread.sleep(100)
            composeTestRule.onNodeWithContentDescription("暂停").performClick()
            Thread.sleep(100)
        }

        // 应用应该保持响应
        composeTestRule.onNodeWithText("圣经闹钟").assertIsDisplayed()
    }

    /**
     * 设置测试数据
     */
    private suspend fun setupTestData() {
        // 清理现有数据
        database.clearAllTables()

        // 插入测试诗篇数据
        val testPsalms = listOf(
            Psalm(1, 1, "诗篇第一篇", "有福的人", "/test/psalm1.mp3", true),
            Psalm(2, 23, "诗篇二十三篇", "耶和华是我的牧者", "/test/psalm23.mp3", true),
            Psalm(3, 91, "诗篇九十一篇", "住在至高者隐密处", "/test/psalm91.mp3", true),
            Psalm(4, 150, "诗篇一百五十篇", "赞美诗", "/test/psalm150.mp3", true)
        )

        testPsalms.forEach { psalm ->
            database.psalmDao().insertPsalm(psalm)
        }

        // 插入测试闹钟数据
        val testAlarms = listOf(
            Alarm(
                id = 1,
                hour = 7,
                minute = 0,
                label = "晨祷时间",
                isEnabled = true,
                repeatDays = listOf(1, 2, 3, 4, 5),
                isSnoozeEnabled = true,
                snoozeDuration = 10,
                isVibrateEnabled = true,
                volume = 0.8f
            ),
            Alarm(
                id = 2,
                hour = 21,
                minute = 30,
                label = "晚祷时间",
                isEnabled = false,
                repeatDays = listOf(0, 6),
                isSnoozeEnabled = false,
                snoozeDuration = 5,
                isVibrateEnabled = false,
                volume = 0.6f
            )
        )

        testAlarms.forEach { alarm ->
            database.alarmDao().insertAlarm(alarm)
        }
    }
}