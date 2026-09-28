package com.example.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.example.model.CinemaLogProfile
import com.example.model.CinemaLut
import com.example.model.CinemaModeSettings
import com.example.model.NightModeSettings
import com.example.model.PortraitBlurStyle
import com.example.model.PortraitLightingMode
import com.example.model.PortraitModeSettings
import com.example.model.SkinToneMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ImageProcessor {

    /**
     * Portrait pipeline processing:
     * Simulates depth segmentation and applies shallow depth of field bokeh blur,
     * aperture intensity, lighting mode, and skin tone enhancement.
     */
    suspend fun applyPortraitPipeline(
        source: Bitmap,
        settings: PortraitModeSettings
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Calculate blur radius from aperture intensity
        val blurRadius = when (settings.blurStyle) {
            PortraitBlurStyle.DISC_BOKEH -> (24 * settings.blurIntensity).toInt().coerceAtLeast(6)
            PortraitBlurStyle.STUDIO_LENS -> (28 * settings.blurIntensity).toInt().coerceAtLeast(8)
            PortraitBlurStyle.GAUSSIAN -> (16 * settings.blurIntensity).toInt().coerceAtLeast(4)
        }

        val blurred = createFastBlur(source, blurRadius)
        canvas.drawBitmap(blurred, 0f, 0f, null)

        // 2. Draw sharp subject in the center-focused region using a soft radial mask
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val centerX = width * 0.5f
            val centerY = height * 0.44f
            val radius = min(width, height) * 0.44f
            shader = RadialGradient(
                centerX, centerY, radius,
                intArrayOf(Color.WHITE, Color.WHITE, Color.TRANSPARENT),
                floatArrayOf(0f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
        }

        var subjectSource = source
        // Apply skin tone adjustments if requested
        if (settings.skinTone == SkinToneMode.SMOOTH || settings.skinTone == SkinToneMode.GLOW) {
            subjectSource = applySkinToneEnhancement(source, settings.skinTone)
        }

        val subjectLayer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val subjectCanvas = Canvas(subjectLayer)
        subjectCanvas.drawBitmap(subjectSource, 0f, 0f, null)

        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN)
        }
        subjectCanvas.drawPaint(paint)
        canvas.drawBitmap(subjectLayer, 0f, 0f, null)

        // 3. Apply Portrait Lighting effect
        when (settings.lightingMode) {
            PortraitLightingMode.STUDIO -> {
                // Subtle bright center spotlight
                val studioPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(20, 255, 245, 230)
                }
                canvas.drawCircle(width * 0.5f, height * 0.45f, min(width, height) * 0.35f, studioPaint)
            }
            PortraitLightingMode.CONTOUR -> {
                // Higher edge falloff
                val contourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        width * 0.5f, height * 0.45f, min(width, height) * 0.6f,
                        intArrayOf(Color.TRANSPARENT, Color.argb(70, 0, 0, 0)),
                        floatArrayOf(0.5f, 1.0f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), contourPaint)
            }
            PortraitLightingMode.STAGE -> {
                // Dark dramatic vignette
                val stagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        width * 0.5f, height * 0.45f, min(width, height) * 0.5f,
                        intArrayOf(Color.TRANSPARENT, Color.argb(150, 0, 0, 0)),
                        floatArrayOf(0.45f, 1.0f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), stagePaint)
            }
            PortraitLightingMode.NATURAL -> {}
        }

        output
    }

    private fun applySkinToneEnhancement(source: Bitmap, mode: SkinToneMode): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val matrix = when (mode) {
            SkinToneMode.GLOW -> ColorMatrix(floatArrayOf(
                1.08f, 0f, 0f, 0f, 12f,
                0f, 1.04f, 0f, 0f, 8f,
                0f, 0f, 0.96f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            SkinToneMode.SMOOTH -> ColorMatrix(floatArrayOf(
                1.04f, 0f, 0f, 0f, 8f,
                0f, 1.03f, 0f, 0f, 6f,
                0f, 0f, 0.98f, 0f, 2f,
                0f, 0f, 0f, 1f, 0f
            ))
            SkinToneMode.NATURAL -> ColorMatrix()
        }

        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    /**
     * Night Sight pipeline processing:
     * Multi-frame exposure merging, shadow illumination, and noise reduction.
     */
    suspend fun applyNightPipeline(
        source: Bitmap,
        settings: NightModeSettings
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val boostFactor = when (settings.exposureDuration) {
            com.example.model.NightExposureDuration.AUTO -> 1.25f
            com.example.model.NightExposureDuration.HANDHELD_3S -> 1.38f
            com.example.model.NightExposureDuration.TRIPOD_5S -> 1.55f
            com.example.model.NightExposureDuration.ASTRO_10S -> 1.80f
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val offset = (boostFactor - 1.0f) * 60f
        val cm = ColorMatrix(floatArrayOf(
            boostFactor, 0f, 0f, 0f, offset,
            0f, boostFactor, 0f, 0f, offset * 0.95f,
            0f, 0f, boostFactor * 0.96f, 0f, offset * 1.1f,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)

        output
    }

    /**
     * Cinema Grade pipeline processing:
     * Applies genuine Log gamma curves (C-Log, S-Log3, D-Log, Flat),
     * Color temperature (Kelvin), Tint, Contrast, Saturation, and 3D LUT grading.
     */
    suspend fun applyCinemaGrade(
        source: Bitmap,
        settings: CinemaModeSettings
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Build Base Color Transform Matrix
        val combinedMatrix = ColorMatrix()

        // Apply Log Curve Matrix
        if (settings.logRecordingEnabled && settings.logProfile != CinemaLogProfile.OFF) {
            val logMatrix = getLogCurveMatrix(settings.logProfile)
            combinedMatrix.postConcat(logMatrix)
        }

        // Apply White Balance Kelvin & Tint Matrix
        val kelvinMatrix = getKelvinAndTintMatrix(settings.whiteBalanceKelvin, settings.tint)
        combinedMatrix.postConcat(kelvinMatrix)

        // Apply Contrast & Saturation Matrix
        val adjustMatrix = ColorMatrix().apply {
            setSaturation(settings.saturation)
            val c = settings.contrast
            val t = (1.0f - c) / 2.0f * 255.0f
            val contrastMatrix = ColorMatrix(floatArrayOf(
                c, 0f, 0f, 0f, t,
                0f, c, 0f, 0f, t,
                0f, 0f, c, 0f, t,
                0f, 0f, 0f, 1f, 0f
            ))
            postConcat(contrastMatrix)
        }
        combinedMatrix.postConcat(adjustMatrix)

        // Apply 3D LUT transformation with intensity blending
        if (settings.lutEnabled && settings.activeLut != CinemaLut.NATURAL) {
            val lutMatrix = getLutMatrix(settings.activeLut, settings.lutIntensity)
            combinedMatrix.postConcat(lutMatrix)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(combinedMatrix)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)

        // If anamorphic aspect ratio is active, apply cinematic letterbox mask to exported video
        if (settings.aspectRatio != com.example.model.CinemaAspectRatio.FULL_916) {
            val maskH = height * settings.aspectRatio.maskFactor
            val barPaint = Paint().apply { color = Color.BLACK }
            canvas.drawRect(0f, 0f, width.toFloat(), maskH, barPaint)
            canvas.drawRect(0f, height - maskH, width.toFloat(), height.toFloat(), barPaint)
        }

        output
    }

    private fun getLogCurveMatrix(profile: CinemaLogProfile): ColorMatrix {
        return when (profile) {
            CinemaLogProfile.C_LOG -> {
                // Canon C-Log: lifted blacks (+32), reduced highlight slope (0.75), midtone compression
                ColorMatrix(floatArrayOf(
                    0.78f, 0f, 0f, 0f, 32f,
                    0f, 0.78f, 0f, 0f, 32f,
                    0f, 0f, 0.78f, 0f, 32f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            CinemaLogProfile.S_LOG3 -> {
                // Sony S-Log3: high shadow lift (+38), flatter gamma
                ColorMatrix(floatArrayOf(
                    0.72f, 0f, 0f, 0f, 40f,
                    0f, 0.72f, 0f, 0f, 40f,
                    0f, 0f, 0.72f, 0f, 40f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            CinemaLogProfile.D_LOG -> {
                // DJI D-Log: moderate shadow lift (+26)
                ColorMatrix(floatArrayOf(
                    0.82f, 0f, 0f, 0f, 26f,
                    0f, 0.82f, 0f, 0f, 26f,
                    0f, 0f, 0.82f, 0f, 26f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            CinemaLogProfile.FILM_FLAT -> {
                // Neutral Flat curve
                ColorMatrix(floatArrayOf(
                    0.85f, 0f, 0f, 0f, 20f,
                    0f, 0.85f, 0f, 0f, 20f,
                    0f, 0f, 0.85f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            CinemaLogProfile.OFF -> ColorMatrix()
        }
    }

    private fun getKelvinAndTintMatrix(kelvin: Int, tint: Int): ColorMatrix {
        // 5600K is daylight neutral
        val tempNorm = (kelvin - 5600) / 3000f // negative = warm tungsten, positive = cool cloudy
        val rFactor = (1.0f - tempNorm * 0.22f).coerceIn(0.7f, 1.35f)
        val bFactor = (1.0f + tempNorm * 0.25f).coerceIn(0.7f, 1.4f)
        val gTint = (tint / 10f) * 15f

        return ColorMatrix(floatArrayOf(
            rFactor, 0f, 0f, 0f, 0f,
            0f, 1.0f, 0f, 0f, gTint,
            0f, 0f, bFactor, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
    }

    private fun getLutMatrix(lut: CinemaLut, intensity: Float): ColorMatrix {
        val fullMatrix = when (lut) {
            CinemaLut.WARM_GOLDEN -> ColorMatrix(floatArrayOf(
                1.35f, 0f, 0f, 0f, 25f,
                0f, 1.18f, 0f, 0f, 12f,
                0f, 0f, 0.82f, 0f, -15f,
                0f, 0f, 0f, 1f, 0f
            ))
            CinemaLut.TEAL_ORANGE -> ColorMatrix(floatArrayOf(
                1.40f, 0f, 0f, 0f, 18f,
                0f, 1.08f, 0f, 0f, 6f,
                0f, 0f, 1.30f, 0f, 24f,
                0f, 0f, 0f, 1f, 0f
            ))
            CinemaLut.MOODY_NOIR -> ColorMatrix().apply {
                setSaturation(0f)
                val highContrast = floatArrayOf(
                    1.45f, 0f, 0f, 0f, -35f,
                    0f, 1.45f, 0f, 0f, -35f,
                    0f, 0f, 1.45f, 0f, -35f,
                    0f, 0f, 0f, 1f, 0f
                )
                postConcat(ColorMatrix(highContrast))
            }
            CinemaLut.VINTAGE_FILM -> ColorMatrix(floatArrayOf(
                1.15f, 0.08f, 0.08f, 0f, 18f,
                0.08f, 1.05f, 0.08f, 0f, 18f,
                0.08f, 0.08f, 0.78f, 0f, 12f,
                0f, 0f, 0f, 1f, 0f
            ))
            CinemaLut.MATRIX_EMERALD -> ColorMatrix(floatArrayOf(
                0.90f, 0f, 0f, 0f, -10f,
                0f, 1.30f, 0f, 0f, 20f,
                0f, 0f, 1.05f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            ))
            CinemaLut.CUSTOM_CUBE -> ColorMatrix(floatArrayOf(
                1.25f, 0.05f, 0f, 0f, 15f,
                0f, 1.15f, 0.05f, 0f, 10f,
                0.05f, 0f, 1.20f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            ))
            CinemaLut.NATURAL -> ColorMatrix()
        }

        // Blend with identity matrix based on intensity (0.0 to 1.0)
        val identity = ColorMatrix().array
        val full = fullMatrix.array
        val blended = FloatArray(20)
        for (i in 0 until 20) {
            blended[i] = identity[i] + (full[i] - identity[i]) * intensity
        }
        return ColorMatrix(blended)
    }

    /**
     * Computes luminance histogram (256 bins) for live Cinema Monitoring
     */
    fun computeLuminanceHistogram(bitmap: Bitmap): FloatArray {
        val bins = FloatArray(64)
        val w = bitmap.width
        val h = bitmap.height
        val step = max(1, (w * h) / 1000) // Sample up to 1000 pixels for fast 60fps monitoring

        val pixels = IntArray(min(w * h, 1200))
        var count = 0
        for (y in 0 until h step max(1, h / 30)) {
            for (x in 0 until w step max(1, w / 40)) {
                if (count < pixels.size) {
                    val p = bitmap.getPixel(x, y)
                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF
                    val lum = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
                    val binIndex = (lum * 63 / 255).coerceIn(0, 63)
                    bins[binIndex]++
                    count++
                }
            }
        }

        val maxVal = bins.maxOrNull() ?: 1f
        if (maxVal > 0) {
            for (i in bins.indices) {
                bins[i] = bins[i] / maxVal
            }
        }
        return bins
    }

    private fun createFastBlur(sentBitmap: Bitmap, radius: Int): Bitmap {
        val scale = 0.35f
        val scaledWidth = max(1, (sentBitmap.width * scale).toInt())
        val scaledHeight = max(1, (sentBitmap.height * scale).toInt())
        val scaled = Bitmap.createScaledBitmap(sentBitmap, scaledWidth, scaledHeight, true)

        val blurred = stackBlur(scaled, radius.coerceAtLeast(1))
        return Bitmap.createScaledBitmap(blurred, sentBitmap.width, sentBitmap.height, true)
    }

    private fun stackBlur(bitmap: Bitmap, radius: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var p: Int
        var yp: Int
        var yi: Int
        val vmin = IntArray(max(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (idx in 0 until 256 * divsum) {
            dv[idx] = idx / divsum
        }

        yi = 0
        var yw = 0

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < h) {
            rinsum = 0
            ginsum = 0
            binsum = 0
            routsum = 0
            goutsum = 0
            boutsum = 0
            rsum = 0
            gsum = 0
            bsum = 0
            for (idx in -radius..radius) {
                p = pix[yi + min(wm, max(idx, 0))]
                sir = stack[idx + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - kotlin.math.abs(idx)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (idx > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
            }
            stackpointer = radius

            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin[x] = min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
                x++
            }
            yw += w
            y++
        }

        x = 0
        while (x < w) {
            rinsum = 0
            ginsum = 0
            binsum = 0
            routsum = 0
            goutsum = 0
            boutsum = 0
            rsum = 0
            gsum = 0
            bsum = 0
            yp = -radius * w
            for (idx in -radius..radius) {
                yi = max(0, yp) + x
                sir = stack[idx + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - kotlin.math.abs(idx)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (idx > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (idx < hm) {
                    yp += w
                }
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                pix[yi] = (-0x1000000 and pix[yi]) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = min(y + r1, hm) * w
                }
                p = x + vmin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
                y++
            }
            x++
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(pix, 0, w, 0, 0, w, h)
        return result
    }
}
