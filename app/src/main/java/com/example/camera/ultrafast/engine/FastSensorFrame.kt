package com.example.camera.ultrafast.engine

import android.graphics.Bitmap

data class FastSensorFrame(
    val frameIndex: Int,
    val timestampNs: Long,
    var bitmap: Bitmap? = null,
    val exposureTimeNs: Long = 0,
    val iso: Int = 100,
    val sharpnessScore: Float = 0f
)
