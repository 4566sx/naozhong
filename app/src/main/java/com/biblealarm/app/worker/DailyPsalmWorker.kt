package com.biblealarm.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.biblealarm.app.manager.PsalmSelectionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 每日诗篇选择工作器
 * 负责在后台定时选择每日诗篇
 */
@HiltWorker
class DailyPsalmWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val psalmSelectionManager: PsalmSelectionManager
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "daily_psalm_selection"
        const val TAG = "DailyPsalmWorker"
    }
    
    override suspend fun doWork(): Result {
        return try {
            // 获取今日诗篇（如果今天还没有选择，会自动选择）
            val todayPsalm = psalmSelectionManager.getTodayPsalm()
            
            if (todayPsalm != null) {
                // 选择成功
                Result.success()
            } else {
                // 没有可用的诗篇
                Result.failure()
            }
        } catch (e: Exception) {
            // 发生异常
            Result.retry()
        }
    }
}