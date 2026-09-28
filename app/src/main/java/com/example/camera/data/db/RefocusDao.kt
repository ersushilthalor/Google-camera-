package com.example.camera.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RefocusDao {
    @Query("SELECT * FROM refocus_photos ORDER BY timestamp DESC")
    fun getAllRefocusPhotos(): Flow<List<RefocusPhotoEntity>>

    @Query("SELECT * FROM refocus_photos WHERE id = :id LIMIT 1")
    suspend fun getPhotoById(id: String): RefocusPhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefocusPhoto(photo: RefocusPhotoEntity)

    @Delete
    suspend fun deleteRefocusPhoto(photo: RefocusPhotoEntity)
}
