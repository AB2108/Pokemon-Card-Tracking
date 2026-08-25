package com.pokemontracker.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * Handles persistence of card photos in the app's private storage.
 *
 * - Camera captures are written to a temporary file in the cache dir (exposed
 *   to the camera app through a [FileProvider] content URI).
 * - Confirmed images are copied into `files/card_images/` and referenced by
 *   their absolute path from the [com.pokemontracker.app.data.Card] entity.
 */
object ImageStorage {

    private const val IMAGES_DIR = "card_images"
    private const val CAMERA_DIR = "camera"

    private fun imagesDir(context: Context): File =
        File(context.filesDir, IMAGES_DIR).apply { mkdirs() }

    private fun cameraDir(context: Context): File =
        File(context.cacheDir, CAMERA_DIR).apply { mkdirs() }

    /** Create a temp file + content URI for the camera app to write into. */
    fun createCameraOutput(context: Context): Pair<Uri, File> {
        val file = File(cameraDir(context), "capture_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return uri to file
    }

    /**
     * Copy the content behind [source] into permanent storage and return the
     * absolute path of the stored file, or null if it could not be read.
     */
    fun persistFromUri(context: Context, source: Uri): String? {
        val target = File(imagesDir(context), "card_${UUID.randomUUID()}.jpg")
        return try {
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            target.absolutePath
        } catch (e: Exception) {
            if (target.exists()) target.delete()
            null
        }
    }

    /** Move an already-captured temp camera file into permanent storage. */
    fun persistFromFile(context: Context, source: File): String? {
        if (!source.exists()) return null
        val target = File(imagesDir(context), "card_${UUID.randomUUID()}.jpg")
        return try {
            source.copyTo(target, overwrite = true)
            source.delete()
            target.absolutePath
        } catch (e: Exception) {
            if (target.exists()) target.delete()
            null
        }
    }

    fun deleteImage(path: String) {
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }
}
