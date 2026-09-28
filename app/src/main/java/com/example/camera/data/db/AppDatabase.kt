package com.example.camera.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.camera.ultrafast.data.UltraFastBurstDao
import com.example.camera.ultrafast.model.UltraFastBurstEntity

@Database(
    entities = [RefocusPhotoEntity::class, UltraFastBurstEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun refocusDao(): RefocusDao
    abstract fun ultraFastBurstDao(): UltraFastBurstDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oppocam_camera.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}
