package com.biblealarm.app.data.database.converter

import androidx.room.TypeConverter
import kotlinx.datetime.DayOfWeek
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room数据库类型转换器
 */
class Converters {
    
    private val gson = Gson()
    
    /**
     * Set<DayOfWeek> 转换为 String
     */
    @TypeConverter
    fun fromDayOfWeekSet(value: Set<DayOfWeek>): String {
        return gson.toJson(value.map { it.name })
    }
    
    /**
     * String 转换为 Set<DayOfWeek>
     */
    @TypeConverter
    fun toDayOfWeekSet(value: String): Set<DayOfWeek> {
        val type = object : TypeToken<List<String>>() {}.type
        val dayNames: List<String> = gson.fromJson(value, type) ?: emptyList()
        return dayNames.mapNotNull { 
            try {
                DayOfWeek.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }.toSet()
    }
    
    /**
     * List<String> 转换为 String
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    /**
     * String 转换为 List<String>
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}