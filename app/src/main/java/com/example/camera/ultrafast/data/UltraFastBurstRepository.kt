package com.example.camera.ultrafast.data

import android.content.Context
import com.example.camera.data.db.AppDatabase
import com.example.camera.ultrafast.model.UltraFastBurstEntity
import kotlinx.coroutines.flow.Flow

class UltraFastBurstRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.ultraFastBurstDao()

    val allBursts: Flow<List<UltraFastBurstEntity>> = dao.getAllBursts()

    suspend fun saveBurstRecord(entity: UltraFastBurstEntity) {
        dao.insertBurst(entity)
    }

    suspend fun deleteBurstRecord(entity: UltraFastBurstEntity) {
        dao.deleteBurst(entity)
    }
}
