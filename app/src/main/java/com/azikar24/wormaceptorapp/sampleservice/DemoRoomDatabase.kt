package com.azikar24.wormaceptorapp.sampleservice

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DemoTaskEntity::class, DemoNoteEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class DemoRoomDatabase : RoomDatabase() {
    abstract fun demoDao(): DemoDao

    companion object {
        private const val DB_NAME = "wormaceptor_demo.db"

        @Volatile
        private var instance: DemoRoomDatabase? = null

        fun get(context: Context): DemoRoomDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DemoRoomDatabase::class.java,
                    DB_NAME,
                ).build().also { instance = it }
            }
        }
    }
}
