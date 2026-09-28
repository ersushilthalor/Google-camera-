package com.example.camera.motionphoto

import java.io.File

data class MotionPhotoResult(
    val photoFile: File,
    val embeddedVideoOffset: Long,
    val embeddedVideoLength: Long,
    val durationMs: Long
)
