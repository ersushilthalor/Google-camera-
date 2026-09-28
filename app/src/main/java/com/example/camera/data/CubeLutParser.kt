package com.example.camera.data

import android.graphics.Bitmap
import android.graphics.Color
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.max
import kotlin.math.min

class CubeLutParser {

    data class Lut3D(
        val title: String,
        val size: Int,
        val data: FloatArray
    ) {
        fun apply(bitmap: Bitmap, intensity: Float = 1.0f): Bitmap {
            val width = bitmap.width
            val height = bitmap.height
            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val maxIndex = size - 1
            for (i in pixels.indices) {
                val p = pixels[i]
                val r = Color.red(p) / 255.0f
                val g = Color.green(p) / 255.0f
                val b = Color.blue(p) / 255.0f

                val ri = (r * maxIndex).toInt().coerceIn(0, maxIndex)
                val gi = (g * maxIndex).toInt().coerceIn(0, maxIndex)
                val bi = (b * maxIndex).toInt().coerceIn(0, maxIndex)

                val index = (bi * size * size + gi * size + ri) * 3
                if (index + 2 < data.size) {
                    val lutR = (data[index] * 255f).coerceIn(0f, 255f)
                    val lutG = (data[index + 1] * 255f).coerceIn(0f, 255f)
                    val lutB = (data[index + 2] * 255f).coerceIn(0f, 255f)

                    val finalR = (Color.red(p) * (1f - intensity) + lutR * intensity).toInt().coerceIn(0, 255)
                    val finalG = (Color.green(p) * (1f - intensity) + lutG * intensity).toInt().coerceIn(0, 255)
                    val finalB = (Color.blue(p) * (1f - intensity) + lutB * intensity).toInt().coerceIn(0, 255)

                    pixels[i] = Color.rgb(finalR, finalG, finalB)
                }
            }

            output.setPixels(pixels, 0, width, 0, 0, width, height)
            return output
        }
    }

    fun parse(inputStream: InputStream, title: String = "Custom LUT"): Lut3D? {
        var lutSize = 0
        val floatList = mutableListOf<Float>()

        try {
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line!!.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

                    if (trimmed.startsWith("LUT_3D_SIZE")) {
                        val parts = trimmed.split("\\s+".toRegex())
                        if (parts.size >= 2) {
                            lutSize = parts[1].toIntOrNull() ?: 17
                        }
                    } else {
                        val tokens = trimmed.split("\\s+".toRegex())
                        if (tokens.size >= 3) {
                            val r = tokens[0].toFloatOrNull()
                            val g = tokens[1].toFloatOrNull()
                            val b = tokens[2].toFloatOrNull()
                            if (r != null && g != null && b != null) {
                                floatList.add(r)
                                floatList.add(g)
                                floatList.add(b)
                            }
                        }
                    }
                }
            }

            if (lutSize > 0 && floatList.size >= lutSize * lutSize * lutSize * 3) {
                return Lut3D(title, lutSize, floatList.toFloatArray())
            }
        } catch (_: Exception) {}

        return null
    }
}
