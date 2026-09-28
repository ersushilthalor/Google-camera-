package com.example.camera.motionphoto

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MotionPhotoEngine(private val context: Context) {

    private val frameBuffer = MotionPhotoFrameBuffer(maxDurationMs = 2000L)
    private val videoEncoder = MotionPhotoVideoEncoder()
    private val xmpPacker = MotionPhotoXmpPacker()

    fun onPreviewFrame(bitmap: Bitmap) {
        frameBuffer.pushFrame(bitmap)
    }

    suspend fun captureMotionPhoto(
        primaryPhoto: Bitmap,
        targetFile: File
    ): MotionPhotoResult? = withContext(Dispatchers.IO) {
        val frames = frameBuffer.snapshotFrames()
        if (frames.isEmpty()) return@withContext null

        val tempJpeg = File(context.cacheDir, "temp_still_${System.currentTimeMillis()}.jpg")
        val tempMp4 = File(context.cacheDir, "temp_video_${System.currentTimeMillis()}.mp4")

        try {
            FileOutputStream(tempJpeg).use { out ->
                primaryPhoto.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            val encoded = videoEncoder.encodeBitmapsToMp4(frames, tempMp4)
            if (!encoded || !tempMp4.exists() || tempMp4.length() == 0L) {
                tempJpeg.copyTo(targetFile, overwrite = true)
                return@withContext null
            }

            val result = xmpPacker.packMotionPhoto(tempJpeg, tempMp4, targetFile)
            tempJpeg.delete()
            tempMp4.delete()
            result
        } catch (_: Exception) {
            null
        }
    }
}
