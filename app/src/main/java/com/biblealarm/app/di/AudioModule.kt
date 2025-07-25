package com.biblealarm.app.di

import android.content.Context
import com.biblealarm.app.data.repository.AudioRepository
import com.biblealarm.app.manager.BuiltInAudioManager
import com.biblealarm.app.manager.PlaybackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 音频相关依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    
    @Provides
    @Singleton
    fun provideBuiltInAudioManager(@ApplicationContext context: Context): BuiltInAudioManager {
        return BuiltInAudioManager(context)
    }
    
    @Provides
    @Singleton
    fun provideAudioRepository(
        @ApplicationContext context: Context,
        builtInAudioManager: BuiltInAudioManager
    ): AudioRepository {
        return AudioRepository(context, builtInAudioManager)
    }
    
    @Provides
    @Singleton
    fun providePlaybackManager(@ApplicationContext context: Context): PlaybackManager {
        return PlaybackManager(context)
    }
}