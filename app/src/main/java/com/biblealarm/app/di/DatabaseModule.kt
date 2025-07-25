package com.biblealarm.app.di

import android.content.Context
import androidx.room.Room
import com.biblealarm.app.data.database.BibleAlarmDatabase
import com.biblealarm.app.data.database.dao.AlarmDao
import com.biblealarm.app.data.database.dao.PsalmDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BibleAlarmDatabase {
        return Room.databaseBuilder(
            context,
            BibleAlarmDatabase::class.java,
            "bible_alarm_database"
        ).build()
    }
    
    @Provides
    fun provideAlarmDao(database: BibleAlarmDatabase): AlarmDao {
        return database.alarmDao()
    }
    
    @Provides
    fun providePsalmDao(database: BibleAlarmDatabase): PsalmDao {
        return database.psalmDao()
    }
}