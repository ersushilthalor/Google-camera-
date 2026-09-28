package com.example.camera.motionphoto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MotionPhotoXmpPacker {

    suspend fun packMotionPhoto(
        jpegFile: File,
        mp4File: File,
        outputFile: File
    ): MotionPhotoResult = withContext(Dispatchers.IO) {
        val jpegBytes = jpegFile.readBytes()
        val mp4Bytes = mp4File.readBytes()

        val videoOffset = jpegBytes.size.toLong()
        val videoLength = mp4Bytes.size.toLong()

        FileOutputStream(outputFile).use { out ->
            out.write(jpegBytes)
            out.write(mp4Bytes)
        }

        MotionPhotoResult(
            photoFile = outputFile,
            embeddedVideoOffset = videoOffset,
            embeddedVideoLength = videoLength,
            durationMs = 2000L
        )
    }

    suspend fun extractEmbeddedVideo(
        motionPhotoFile: File,
        videoOffset: Long,
        videoLength: Long,
        outputMp4File: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            FileInputStream(motionPhotoFile).use { input ->
                input.skip(videoOffset)
                val buffer = ByteArray(8192)
                var remaining = videoLength
                FileOutputStream(outputMp4File).use { output ->
                    while (remaining > 0) {
                        val toRead = minOf(buffer.size.toLong(), remaining).toInt()
                        val read = input.read(buffer, 0, toRead)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        remaining -= read
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
