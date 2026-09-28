package com.example.camera.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refocus_photos")
data class RefocusPhotoEntity(
    @PrimaryKey
    val id: String,
    val filePath: String,
    val depthMapPath: String,
    val focalPointX: Float = 0.5f,
    val focalPointY: Float = 0.5f,
    val apertureValue: Float = 2.0f,
    val blurIntensity: Float = 0.7f,
    val blurStyle: String = "DISC_BOKEH",
    val timestamp: Long = System.currentTimeMillis()
)
