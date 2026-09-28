package com.example.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.CapturedMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MediaRepository(private val context: Context) {

    private val _mediaList = MutableStateFlow<List<CapturedMedia>>(emptyList())
    val mediaList: StateFlow<List<CapturedMedia>> = _mediaList.asStateFlow()

    private val storageDir: File by lazy {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CameraApp")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        val list = mutableListOf<CapturedMedia>()

        // Check if there are existing media files in app directory
        val files = storageDir.listFiles()?.sortedByDescending { it.lastModified() }
        files?.forEach { file ->
            val isVideo = file.name.endsWith(".mp4", ignoreCase = true)
            list.add(
                CapturedMedia(
                    id = file.name,
                    filePath = file.absolutePath,
                    uriString = Uri.fromFile(file).toString(),
                    isVideo = isVideo,
                    timestamp = file.lastModified(),
                    modeName = if (isVideo) "Video" else "Photo"
                )
            )
        }

        // If list is empty, initialize with the generated sample thumb so thumbnail is populated
        if (list.isEmpty()) {
            copyDefaultSampleThumbnail()
        } else {
            _mediaList.value = list
        }
    }

    private suspend fun copyDefaultSampleThumbnail() = withContext(Dispatchers.IO) {
        try {
            // Find sample drawable
            val sampleResId = context.resources.getIdentifier("sample_gallery_thumb_1790560898568", "drawable", context.packageName)
            if (sampleResId != 0) {
                val bitmap = BitmapFactory.decodeResource(context.resources, sampleResId)
                if (bitmap != null) {
                    val file = File(storageDir, "IMG_SAMPLE_PORTRAIT.jpg")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }
                    val sample = CapturedMedia(
                        id = file.name,
                        filePath = file.absolutePath,
                        uriString = Uri.fromFile(file).toString(),
                        isVideo = false,
                        timestamp = System.currentTimeMillis(),
                        modeName = "Portrait"
                    )
                    _mediaList.value = listOf(sample)
                }
            }
        } catch (_: Exception) {
        }
    }

    suspend fun savePhoto(bitmap: Bitmap, modeName: String, orientationDegrees: Int = 0, appliedLut: String? = null): CapturedMedia = withContext(Dispatchers.IO) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "IMG_${timeStamp}_${modeName.replace(" ", "")}.jpg"
        val file = File(storageDir, fileName)

        val rotatedBitmap = if (orientationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(orientationDegrees.toFloat()) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }

        FileOutputStream(file).use { out ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
        }

        // Notify MediaScanner
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/jpeg"), null)

        val media = CapturedMedia(
            id = file.name,
            filePath = file.absolutePath,
            uriString = Uri.fromFile(file).toString(),
            isVideo = false,
            timestamp = System.currentTimeMillis(),
            modeName = modeName,
            appliedLut = appliedLut
        )

        _mediaList.value = listOf(media) + _mediaList.value
        media
    }

    suspend fun saveVideo(videoFile: File, modeName: String, appliedLut: String? = null): CapturedMedia = withContext(Dispatchers.IO) {
        MediaScannerConnection.scanFile(context, arrayOf(videoFile.absolutePath), arrayOf("video/mp4"), null)

        val media = CapturedMedia(
            id = videoFile.name,
            filePath = videoFile.absolutePath,
            uriString = Uri.fromFile(videoFile).toString(),
            isVideo = true,
            timestamp = System.currentTimeMillis(),
            modeName = modeName,
            appliedLut = appliedLut
        )

        _mediaList.value = listOf(media) + _mediaList.value
        media
    }

    fun createVideoOutputFile(modeName: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "VID_${timeStamp}_${modeName.replace(" ", "")}.mp4"
        return File(storageDir, fileName)
    }

    suspend fun deleteMedia(media: CapturedMedia) = withContext(Dispatchers.IO) {
        val file = File(media.filePath)
        if (file.exists()) {
            file.delete()
        }
        _mediaList.value = _mediaList.value.filter { it.id != media.id }
    }
}
