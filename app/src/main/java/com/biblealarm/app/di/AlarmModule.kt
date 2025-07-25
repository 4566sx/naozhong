package com.biblealarm.app.di

import android.content.Context
import com.biblealarm.app.data.repository.AlarmRepository
import com.biblealarm.app.data.repository.SettingsRepository
import com.biblealarm.app.data.database.dao.AlarmDao
import com.biblealarm.app.manager.AlarmManager
import com.biblealarm.app.manager.PermissionManager
import com.biblealarm.app.manager.SnoozeManager
import com.biblealarm.app.manager.VolumeManager
import com.biblealarm.app.manager.WorkManagerScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 闹钟相关依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {
    
    @Provides
    @Singleton
    fun provideAlarmRepository(alarmDao: AlarmDao): AlarmRepository {
        return AlarmRepository(alarmDao)
    }
    
    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }
    
    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return AlarmManager(context)
    }
    
    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context): PermissionManager {
        return PermissionManager(context)
    }
    
    @Provides
    @Singleton
    fun provideSnoozeManager(@ApplicationContext context: Context): SnoozeManager {
        return SnoozeManager(context)
    }
    
    @Provides
    @Singleton
    fun provideVolumeManager(@ApplicationContext context: Context): VolumeManager {
        return VolumeManager(context)
    }
    
    @Provides
    @Singleton
    fun provideWorkManagerScheduler(@ApplicationContext context: Context): WorkManagerScheduler {
        return WorkManagerScheduler(context)
    }
}