package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SkinRoutineDao
import com.example.data.local.entity.JournalEntryEntity
import com.example.data.local.entity.PreferencesEntity
import com.example.data.local.entity.RoutineStepEntity
import com.example.data.local.entity.ShelfEntity

@Database(
    entities = [
        PreferencesEntity::class,
        ShelfEntity::class,
        RoutineStepEntity::class,
        JournalEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SkinRoutineDatabase : RoomDatabase() {

    abstract fun skinRoutineDao(): SkinRoutineDao

    companion object {
        @Volatile
        private var INSTANCE: SkinRoutineDatabase? = null

        fun getDatabase(context: Context): SkinRoutineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkinRoutineDatabase::class.java,
                    "skin_routine_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
