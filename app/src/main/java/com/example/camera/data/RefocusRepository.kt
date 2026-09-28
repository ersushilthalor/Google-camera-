package com.example.camera.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.camera.data.db.AppDatabase
import com.example.camera.data.db.RefocusPhotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RefocusRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.refocusDao()
    private val refocusDir = File(context.filesDir, "refocus_photos").apply { mkdirs() }

    val allPhotos: Flow<List<RefocusPhotoEntity>> = dao.getAllRefocusPhotos()

    suspend fun saveRefocusCapture(
        baseImage: Bitmap,
        depthMap: Bitmap,
        focalX: Float = 0.5f,
        focalY: Float = 0.5f
    ): RefocusPhotoEntity = withContext(Dispatchers.IO) {
        val id = "refocus_${System.currentTimeMillis()}"
        val imageFile = File(refocusDir, "${id}_base.jpg")
        val depthFile = File(refocusDir, "${id}_depth.png")

        FileOutputStream(imageFile).use { out ->
            baseImage.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        FileOutputStream(depthFile).use { out ->
            depthMap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val entity = RefocusPhotoEntity(
            id = id,
            filePath = imageFile.absolutePath,
            depthMapPath = depthFile.absolutePath,
            focalPointX = focalX,
            focalPointY = focalY
        )
        dao.insertRefocusPhoto(entity)
        entity
    }

    suspend fun loadPhotoBitmaps(entity: RefocusPhotoEntity): Pair<Bitmap?, Bitmap?> = withContext(Dispatchers.IO) {
        val base = BitmapFactory.decodeFile(entity.filePath)
        val depth = BitmapFactory.decodeFile(entity.depthMapPath)
        Pair(base, depth)
    }

    suspend fun updateFocus(entity: RefocusPhotoEntity, newX: Float, newY: Float, blur: Float, aperture: Float) = withContext(Dispatchers.IO) {
        val updated = entity.copy(
            focalPointX = newX,
            focalPointY = newY,
            blurIntensity = blur,
            apertureValue = aperture
        )
        dao.insertRefocusPhoto(updated)
    }
}
