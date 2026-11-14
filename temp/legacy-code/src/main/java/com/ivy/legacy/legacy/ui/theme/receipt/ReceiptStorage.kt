package com.ivy.legacy.ui.theme.receipt

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * App-private storage for ledger receipts (photos).
 *
 * Files live in `filesDir/receipts/` — never in the shared gallery, never uploaded.
 * Only a relative path is persisted in the database, so a backup can never leak
 * someone else's photo.
 */
object ReceiptStorage {
    private const val DIR = "receipts"
    private const val MIME = "image/jpeg"

    fun receiptsDir(context: Context): File =
        File(context.filesDir, DIR).apply { if (!exists()) mkdirs() }

    /** Relative path stored in the DB, e.g. `receipts/<uuid>.jpg`. */
    fun relativePath(file: File): String = "${file.parentFile?.name}/${file.name}"

    fun fileFor(context: Context, relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        val name = relativePath.substringAfterLast('/')
        if (name.isBlank() || name.contains("..")) return null
        return File(receiptsDir(context), name)
    }

    /**
     * Copies the content behind [source] into the private receipts folder and
     * returns the relative path (or null when it fails).
     */
    suspend fun saveFrom(
        context: Context,
        source: Uri,
    ): String? = withContext(Dispatchers.IO) {
        try {
            val target = File(receiptsDir(context), "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            relativePath(target)
        } catch (e: Exception) {
            null
        }
    }

    /** Creates a new (empty) receipt file and returns a shareable URI for the camera. */
    fun newCameraUri(context: Context): Pair<Uri, File>? {
        return try {
            val target = File(receiptsDir(context), "${UUID.randomUUID()}.jpg")
            target.createNewFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                target
            )
            uri to target
        } catch (e: Exception) {
            null
        }
    }

    /** Removes a stored receipt (lazy cleanup when a record is deleted). */
    fun delete(context: Context, relativePath: String?) {
        val file = fileFor(context, relativePath) ?: return
        try {
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            // best effort
        }
    }

    fun mimeType(): String = MIME
}
