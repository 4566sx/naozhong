package com.biblealarm.app.di

import android.content.Context
import com.biblealarm.app.data.repository.AudioRepository
import com.biblealarm.app.data.database.dao.PsalmDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 音频模块依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    
    @Provides
    @Singleton
    fun provideAudioRepository(
        @ApplicationContext context: Context,
        psalmDao: PsalmDao
    ): AudioRepository {
        return AudioRepository(context, psalmDao)
    }
}