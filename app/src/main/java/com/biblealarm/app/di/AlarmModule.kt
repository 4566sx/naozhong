package com.biblealarm.app.di

import android.content.Context
import com.biblealarm.app.data.database.dao.AlarmDao
import com.biblealarm.app.data.repository.AlarmRepository
import com.biblealarm.app.manager.AlarmManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 闹钟模块依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {
    
    @Provides
    @Singleton
    fun provideAlarmManager(
        @ApplicationContext context: Context
    ): AlarmManager {
        return AlarmManager(context)
    }
    
    @Provides
    @Singleton
    fun provideAlarmRepository(
        alarmDao: AlarmDao,
        alarmManager: AlarmManager
    ): AlarmRepository {
        return AlarmRepository(alarmDao, alarmManager)
    }
}