package com.example.camera.ultrafast.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ultra_fast_bursts")
data class UltraFastBurstEntity(
    @PrimaryKey
    val id: String,
    val folderPath: String,
    val frameCount: Int,
    val bestFrameIndex: Int = 0,
    val captureFps: Int = 30,
    val durationMs: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
