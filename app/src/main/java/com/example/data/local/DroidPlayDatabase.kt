package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ControlProfileEntity::class, GameLibraryEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DroidPlayDatabase : RoomDatabase() {
    abstract fun droidPlayDao(): DroidPlayDao

    companion object {
        @Volatile
        private var INSTANCE: DroidPlayDatabase? = null

        fun getInstance(context: Context): DroidPlayDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DroidPlayDatabase::class.java,
                    "droidplay_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
