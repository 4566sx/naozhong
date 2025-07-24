package com.biblealarm.app.manager

import android.content.Context
import androidx.work.*
import com.biblealarm.app.worker.DailyPsalmWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 工作管理器调度器
 * 负责调度后台任务
 */
@Singleton
class WorkManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * 调度每日诗篇选择任务
     */
    fun scheduleDailyPsalmSelection() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setRequiresStorageNotLow(false)
            .build()
        
        val dailyPsalmRequest = PeriodicWorkRequestBuilder<DailyPsalmWorker>(
            1, TimeUnit.DAYS,  // 每天执行一次
            15, TimeUnit.MINUTES  // 灵活间隔15分钟
        )
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .addTag(DailyPsalmWorker.TAG)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            DailyPsalmWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyPsalmRequest
        )
    }
    
    /**
     * 取消每日诗篇选择任务
     */
    fun cancelDailyPsalmSelection() {
        workManager.cancelUniqueWork(DailyPsalmWorker.WORK_NAME)
    }
    
    /**
     * 立即执行诗篇选择任务
     */
    fun executePsalmSelectionNow() {
        val immediateRequest = OneTimeWorkRequestBuilder<DailyPsalmWorker>()
            .addTag(DailyPsalmWorker.TAG)
            .build()
        
        workManager.enqueue(immediateRequest)
    }
    
    /**
     * 计算初始延迟时间
     * 设置为每天凌晨0点执行
     */
    private fun calculateInitialDelay(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // 如果当前时间已经过了今天的0点，则设置为明天的0点
            if (timeInMillis <= currentTime) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return calendar.timeInMillis - currentTime
    }
    
    /**
     * 获取工作状态
     */
    fun getDailyPsalmWorkStatus(): androidx.lifecycle.LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(DailyPsalmWorker.WORK_NAME)
    }
    
    /**
     * 检查是否已调度每日任务
     */
    suspend fun isDailyPsalmScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(DailyPsalmWorker.WORK_NAME).await()
        return workInfos.any { workInfo ->
            workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING
        }
    }
}