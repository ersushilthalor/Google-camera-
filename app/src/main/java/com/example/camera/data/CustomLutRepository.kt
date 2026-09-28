package com.example.camera.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CustomLutRepository(private val context: Context) {

    private val lutsDir = File(context.filesDir, "custom_luts").apply { mkdirs() }
    private val parser = CubeLutParser()

    suspend fun importLutFile(uri: Uri, displayName: String): CubeLutParser.Lut3D? = withContext(Dispatchers.IO) {
        try {
            val destinationFile = File(lutsDir, "${System.currentTimeMillis()}_$displayName.cube")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (destinationFile.exists()) {
                destinationFile.inputStream().use { stream ->
                    parser.parse(stream, displayName)
                }
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun listSavedLuts(): List<File> = withContext(Dispatchers.IO) {
        lutsDir.listFiles { _, name -> name.endsWith(".cube") }?.toList() ?: emptyList()
    }
}
