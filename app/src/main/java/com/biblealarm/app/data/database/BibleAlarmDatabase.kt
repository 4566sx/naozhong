package com.biblealarm.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.biblealarm.app.data.model.Alarm
import com.biblealarm.app.data.model.Psalm
import com.biblealarm.app.data.database.dao.AlarmDao
import com.biblealarm.app.data.database.dao.PsalmDao
import com.biblealarm.app.data.database.converter.Converters

/**
 * 圣经闹钟应用数据库
 */
@Database(
    entities = [Alarm::class, Psalm::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BibleAlarmDatabase : RoomDatabase() {
    
    abstract fun alarmDao(): AlarmDao
    abstract fun psalmDao(): PsalmDao
    
    companion object {
        const val DATABASE_NAME = "bible_alarm_database"
        
        @Volatile
        private var INSTANCE: BibleAlarmDatabase? = null
        
        fun getDatabase(context: Context): BibleAlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BibleAlarmDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}