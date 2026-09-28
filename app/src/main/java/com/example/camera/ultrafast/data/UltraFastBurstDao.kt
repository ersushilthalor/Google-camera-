package com.example.camera.ultrafast.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.camera.ultrafast.model.UltraFastBurstEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UltraFastBurstDao {
    @Query("SELECT * FROM ultra_fast_bursts ORDER BY timestamp DESC")
    fun getAllBursts(): Flow<List<UltraFastBurstEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBurst(burst: UltraFastBurstEntity)

    @Delete
    suspend fun deleteBurst(burst: UltraFastBurstEntity)
}
