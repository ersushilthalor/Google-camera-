package com.example.camera.motionphoto

import android.graphics.Bitmap
import java.util.concurrent.ConcurrentLinkedDeque

class MotionPhotoFrameBuffer(private val maxDurationMs: Long = 2000L) {

    data class TimestampedFrame(val timestampMs: Long, val bitmap: Bitmap)

    private val frameDeque = ConcurrentLinkedDeque<TimestampedFrame>()

    fun pushFrame(bitmap: Bitmap) {
        val now = System.currentTimeMillis()
        val copy = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        frameDeque.addLast(TimestampedFrame(now, copy))

        // Evict older frames beyond maxDurationMs
        while (frameDeque.isNotEmpty()) {
            val oldest = frameDeque.first
            if (now - oldest.timestampMs > maxDurationMs) {
                frameDeque.pollFirst()?.bitmap?.recycle()
            } else {
                break
            }
        }
    }

    fun snapshotFrames(): List<Bitmap> {
        return frameDeque.map { it.bitmap }
    }

    fun clear() {
        while (frameDeque.isNotEmpty()) {
            frameDeque.pollFirst()?.bitmap?.recycle()
        }
    }
}
