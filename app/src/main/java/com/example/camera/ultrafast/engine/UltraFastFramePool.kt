package com.example.camera.ultrafast.engine

import android.graphics.Bitmap
import java.util.concurrent.ConcurrentLinkedQueue

class UltraFastFramePool(
    private val poolSize: Int = 30,
    private val width: Int = 1920,
    private val height: Int = 1080
) {
    private val availableBitmaps = ConcurrentLinkedQueue<Bitmap>()

    init {
        for (i in 0 until minOf(poolSize, 10)) {
            availableBitmaps.add(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888))
        }
    }

    fun obtain(): Bitmap {
        return availableBitmaps.poll() ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun recycle(bitmap: Bitmap) {
        if (!bitmap.isRecycled && availableBitmaps.size < poolSize) {
            availableBitmaps.add(bitmap)
        } else {
            bitmap.recycle()
        }
    }

    fun clear() {
        while (availableBitmaps.isNotEmpty()) {
            availableBitmaps.poll()?.recycle()
        }
    }
}
