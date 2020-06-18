package com.yausername.dvd.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Download::class), version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadsDao(): DownloadsDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dvd_db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
