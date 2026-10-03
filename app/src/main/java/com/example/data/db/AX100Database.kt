package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PatchPresetEntity::class,
        MidiMappingEntity::class,
        ZFileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AX100Database : RoomDatabase() {
    abstract fun dao(): AX100Dao

    companion object {
        @Volatile
        private var INSTANCE: AX100Database? = null

        fun getDatabase(context: Context): AX100Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AX100Database::class.java,
                    "ax100_zfile_daw.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
