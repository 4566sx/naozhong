package com.biblealarm.app.manager

import com.biblealarm.app.data.model.Psalm
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.time.LocalDate

/**
 * 诗篇选择管理器测试
 */
class PsalmSelectionManagerTest {

    @Mock
    private lateinit var mockPsalmRepository: com.biblealarm.app.data.repository.PsalmRepository

    private lateinit var psalmSelectionManager: PsalmSelectionManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        psalmSelectionManager = PsalmSelectionManager(mockPsalmRepository)
    }

    @Test
    fun `测试每日诗篇选择的一致性`() = runTest {
        // 准备测试数据
        val testPsalms = (1..150).map { number ->
            Psalm(
                id = number.toLong(),
                number = number,
                title = "诗篇 $number",
                content = "测试内容 $number",
                audioPath = "/test/psalm_$number.mp3",
                isAvailable = true
            )
        }

        whenever(mockPsalmRepository.getAllPsalms()).thenReturn(testPsalms)

        val today = LocalDate.now()
        
        // 多次调用应该返回相同的诗篇
        val psalm1 = psalmSelectionManager.getTodaysPsalm()
        val psalm2 = psalmSelectionManager.getTodaysPsalm()
        val psalm3 = psalmSelectionManager.getTodaysPsalm()

        assertNotNull("今日诗篇不应为空", psalm1)
        assertEquals("多次调用应返回相同诗篇", psalm1?.number, psalm2?.number)
        assertEquals("多次调用应返回相同诗篇", psalm1?.number, psalm3?.number)
    }

    @Test
    fun `测试不同日期选择不同诗篇`() = runTest {
        // 准备测试数据
        val testPsalms = (1..150).map { number ->
            Psalm(
                id = number.toLong(),
                number = number,
                title = "诗篇 $number",
                content = "测试内容 $number",
                audioPath = "/test/psalm_$number.mp3",
                isAvailable = true
            )
        }

        whenever(mockPsalmRepository.getAllPsalms()).thenReturn(testPsalms)

        // 获取今天和明天的诗篇
        val todayPsalm = psalmSelectionManager.getTodaysPsalm()
        val tomorrowPsalm = psalmSelectionManager.getPsalmForDate(LocalDate.now().plusDays(1))

        assertNotNull("今日诗篇不应为空", todayPsalm)
        assertNotNull("明日诗篇不应为空", tomorrowPsalm)
        assertNotEquals("不同日期应选择不同诗篇", todayPsalm?.number, tomorrowPsalm?.number)
    }

    @Test
    fun `测试随机选择范围正确性`() = runTest {
        // 准备测试数据
        val testPsalms = (1..150).map { number ->
            Psalm(
                id = number.toLong(),
                number = number,
                title = "诗篇 $number",
                content = "测试内容 $number",
                audioPath = "/test/psalm_$number.mp3",
                isAvailable = true
            )
        }

        whenever(mockPsalmRepository.getAllPsalms()).thenReturn(testPsalms)

        // 测试多个日期的选择结果
        val selectedNumbers = mutableSetOf<Int>()
        val testDate = LocalDate.now()
        
        for (i in 0..30) {
            val psalm = psalmSelectionManager.getPsalmForDate(testDate.plusDays(i.toLong()))
            psalm?.let { selectedNumbers.add(it.number) }
        }

        // 验证选择的诗篇编号都在有效范围内
        selectedNumbers.forEach { number ->
            assertTrue("诗篇编号应在1-150范围内", number in 1..150)
        }
        
        // 验证有一定的随机性（至少选择了5个不同的诗篇）
        assertTrue("应该有足够的随机性", selectedNumbers.size >= 5)
    }

    @Test
    fun `测试空诗篇列表处理`() = runTest {
        whenever(mockPsalmRepository.getAllPsalms()).thenReturn(emptyList())

        val psalm = psalmSelectionManager.getTodaysPsalm()
        
        assertNull("空列表应返回null", psalm)
    }

    @Test
    fun `测试获取相邻诗篇`() = runTest {
        // 准备测试数据
        val testPsalms = (1..150).map { number ->
            Psalm(
                id = number.toLong(),
                number = number,
                title = "诗篇 $number",
                content = "测试内容 $number",
                audioPath = "/test/psalm_$number.mp3",
                isAvailable = true
            )
        }

        whenever(mockPsalmRepository.getAllPsalms()).thenReturn(testPsalms)

        val currentPsalm = testPsalms[49] // 诗篇50
        
        val nextPsalm = psalmSelectionManager.getNextPsalm(currentPsalm)
        val previousPsalm = psalmSelectionManager.getPreviousPsalm(currentPsalm)

        assertEquals("下一篇应该是诗篇51", 51, nextPsalm?.number)
        assertEquals("上一篇应该是诗篇49", 49, previousPsalm?.number)
    }

    @Test
    fun `测试边界诗篇处理`() = runTest {
        // 准备测试数据
        val testPsalms = (1..150).map { number ->
            Psalm(
                id = number.toLong(),
                number = number,
                title = "诗篇 $number",
                content = "测试内容 $number",
                audioPath = "/test/psalm_$number.mp3",
                isAvailable = true
            )
        }

        whenever(mockPsalmRepository.getAllPsalms()).thenReturn(testPsalms)

        val firstPsalm = testPsalms[0] // 诗篇1
        val lastPsalm = testPsalms[149] // 诗篇150
        
        val nextAfterLast = psalmSelectionManager.getNextPsalm(lastPsalm)
        val previousBeforeFirst = psalmSelectionManager.getPreviousPsalm(firstPsalm)

        assertEquals("诗篇150的下一篇应该是诗篇1", 1, nextAfterLast?.number)
        assertEquals("诗篇1的上一篇应该是诗篇150", 150, previousBeforeFirst?.number)
    }
}