package com.biblealarm.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 设置数据仓库
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val AUDIO_PATH = stringPreferencesKey("audio_path")
        private val DEFAULT_VOLUME = floatPreferencesKey("default_volume")
        private val FADE_IN_ENABLED = booleanPreferencesKey("fade_in_enabled")
        private val SNOOZE_DURATION = intPreferencesKey("snooze_duration")
        private val VIBRATE_ENABLED = booleanPreferencesKey("vibrate_enabled")
        private val ALARM_TIMEOUT = intPreferencesKey("alarm_timeout")
        private val DAILY_PSALM_ENABLED = booleanPreferencesKey("daily_psalm_enabled")
        private val PSALM_SELECTION_HOUR = intPreferencesKey("psalm_selection_hour")
        private val PSALM_SELECTION_MINUTE = intPreferencesKey("psalm_selection_minute")
        private val RANDOM_SEED_RESET = booleanPreferencesKey("random_seed_reset")
        private val AUTO_START_ENABLED = booleanPreferencesKey("auto_start_enabled")
        private val USE_BUILT_IN_AUDIO = booleanPreferencesKey("use_built_in_audio")
    }
    
    /**
     * 获取音频路径
     */
    fun getAudioPath(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[AUDIO_PATH] ?: ""
        }
    }
    
    /**
     * 设置音频路径
     */
    suspend fun setAudioPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[AUDIO_PATH] = path
        }
    }
    
    /**
     * 获取默认音量
     */
    fun getDefaultVolume(): Flow<Float> {
        return context.dataStore.data.map { preferences ->
            preferences[DEFAULT_VOLUME] ?: 0.7f
        }
    }
    
    /**
     * 设置默认音量
     */
    suspend fun setDefaultVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_VOLUME] = volume
        }
    }
    
    /**
     * 获取渐强播放设置
     */
    fun getFadeInEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[FADE_IN_ENABLED] ?: true
        }
    }
    
    /**
     * 设置渐强播放
     */
    suspend fun setFadeInEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FADE_IN_ENABLED] = enabled
        }
    }
    
    /**
     * 获取贪睡时长
     */
    fun getSnoozeDuration(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[SNOOZE_DURATION] ?: 10
        }
    }
    
    /**
     * 设置贪睡时长
     */
    suspend fun setSnoozeDuration(duration: Int) {
        context.dataStore.edit { preferences ->
            preferences[SNOOZE_DURATION] = duration
        }
    }
    
    /**
     * 获取振动设置
     */
    fun getVibrateEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[VIBRATE_ENABLED] ?: true
        }
    }
    
    /**
     * 设置振动
     */
    suspend fun setVibrateEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATE_ENABLED] = enabled
        }
    }
    
    /**
     * 获取闹钟超时时间
     */
    fun getAlarmTimeout(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[ALARM_TIMEOUT] ?: 5
        }
    }
    
    /**
     * 设置闹钟超时时间
     */
    suspend fun setAlarmTimeout(timeout: Int) {
        context.dataStore.edit { preferences ->
            preferences[ALARM_TIMEOUT] = timeout
        }
    }
    
    /**
     * 获取每日诗篇自动选择设置
     */
    fun getDailyPsalmEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DAILY_PSALM_ENABLED] ?: true
        }
    }
    
    /**
     * 设置每日诗篇自动选择
     */
    suspend fun setDailyPsalmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_PSALM_ENABLED] = enabled
        }
    }
    
    /**
     * 获取诗篇选择时间
     */
    fun getPsalmSelectionTime(): Flow<Pair<Int, Int>> {
        return context.dataStore.data.map { preferences ->
            val hour = preferences[PSALM_SELECTION_HOUR] ?: 6
            val minute = preferences[PSALM_SELECTION_MINUTE] ?: 0
            Pair(hour, minute)
        }
    }
    
    /**
     * 设置诗篇选择时间
     */
    suspend fun setPsalmSelectionTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PSALM_SELECTION_HOUR] = hour
            preferences[PSALM_SELECTION_MINUTE] = minute
        }
    }
    
    /**
     * 获取随机种子重置设置
     */
    fun getRandomSeedReset(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[RANDOM_SEED_RESET] ?: true
        }
    }
    
    /**
     * 设置随机种子重置
     */
    suspend fun setRandomSeedReset(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RANDOM_SEED_RESET] = enabled
        }
    }
    
    /**
     * 获取开机自启设置
     */
    fun getAutoStartEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[AUTO_START_ENABLED] ?: false
        }
    }
    
    /**
     * 设置开机自启
     */
    suspend fun setAutoStartEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_START_ENABLED] = enabled
        }
    }
    
    /**
     * 获取是否使用内置音频
     */
    fun getUseBuiltInAudio(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[USE_BUILT_IN_AUDIO] ?: true
        }
    }
    
    /**
     * 设置是否使用内置音频
     */
    suspend fun setUseBuiltInAudio(useBuiltIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_BUILT_IN_AUDIO] = useBuiltIn
        }
    }
    
    /**
     * 重置所有设置到默认值
     */
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}