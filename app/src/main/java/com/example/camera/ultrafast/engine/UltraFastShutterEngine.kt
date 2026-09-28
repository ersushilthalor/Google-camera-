package com.example.camera.ultrafast.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

class UltraFastShutterEngine(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val isBursting = AtomicBoolean(false)
    private val framePool = UltraFastFramePool()
    private val capturedFrames = mutableListOf<FastSensorFrame>()

    private val _liveBurstCount = MutableStateFlow(0)
    val liveBurstCount: StateFlow<Int> = _liveBurstCount.asStateFlow()

    private val _isCapturingBurst = MutableStateFlow(false)
    val isCapturingBurst: StateFlow<Boolean> = _isCapturingBurst.asStateFlow()

    fun startBurst(frameProvider: () -> Bitmap?) {
        if (isBursting.getAndSet(true)) return
        capturedFrames.clear()
        _liveBurstCount.value = 0
        _isCapturingBurst.value = true

        coroutineScope.launch(Dispatchers.Default) {
            var index = 0
            val startTime = System.currentTimeMillis()
            while (isBursting.get() && index < 50) {
                val frameTime = System.currentTimeMillis()
                val liveBmp = frameProvider()
                val targetBmp = framePool.obtain()

                if (liveBmp != null && !liveBmp.isRecycled) {
                    val canvas = Canvas(targetBmp)
                    canvas.drawBitmap(liveBmp, 0f, 0f, null)
                } else {
                    val canvas = Canvas(targetBmp)
                    canvas.drawColor(Color.rgb(40, 45, 50))
                    val p = Paint().apply { color = Color.WHITE; textSize = 32f }
                    canvas.drawText("BURST FRAME #$index", 80f, 200f, p)
                }

                val frame = FastSensorFrame(
                    frameIndex = index,
                    timestampNs = System.nanoTime(),
                    bitmap = targetBmp,
                    exposureTimeNs = 1_000_000L, // 1/1000s fast shutter
                    iso = 200,
                    sharpnessScore = computeSharpness(targetBmp)
                )
                capturedFrames.add(frame)
                index++
                _liveBurstCount.value = index

                delay(33) // ~30 fps rapid fire burst
            }
            _isCapturingBurst.value = false
        }
    }

    fun stopBurst(onComplete: (burstId: String, count: Int, bestIndex: Int) -> Unit) {
        if (!isBursting.getAndSet(false)) return
        _isCapturingBurst.value = false

        coroutineScope.launch(Dispatchers.IO) {
            val burstId = "burst_${System.currentTimeMillis()}"
            val burstDir = File(context.filesDir, burstId).apply { mkdirs() }

            var bestIndex = 0
            var highestSharpness = -1f

            capturedFrames.forEachIndexed { i, frame ->
                frame.bitmap?.let { bmp ->
                    val file = File(burstDir, "frame_${String.format("%03d", i)}.jpg")
                    FileOutputStream(file).use { out ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    if (frame.sharpnessScore > highestSharpness) {
                        highestSharpness = frame.sharpnessScore
                        bestIndex = i
                    }
                    framePool.recycle(bmp)
                }
            }

            val total = capturedFrames.size
            capturedFrames.clear()
            _liveBurstCount.value = 0

            withContext(Dispatchers.Main) {
                onComplete(burstId, total, bestIndex)
            }
        }
    }

    private fun computeSharpness(bitmap: Bitmap): Float {
        // Compute Laplacian gradient approximation on center crop
        val w = minOf(bitmap.width, 300)
        val h = minOf(bitmap.height, 300)
        var totalGrad = 0L
        for (y in 10 until h - 10 step 10) {
            for (x in 10 until w - 10 step 10) {
                val p = bitmap.getPixel(x, y)
                val pNext = bitmap.getPixel(x + 2, y)
                val diff = Math.abs((p and 0xFF) - (pNext and 0xFF))
                totalGrad += diff
            }
        }
        return totalGrad.toFloat()
    }
}
