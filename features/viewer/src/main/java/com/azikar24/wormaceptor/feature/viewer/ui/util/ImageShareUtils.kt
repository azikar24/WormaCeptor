package com.azikar24.wormaceptor.feature.viewer.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection.scanFile
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.azikar24.wormaceptor.feature.viewer.R
import java.io.File
import java.io.FileOutputStream

private const val WORMACEPTOR_DIR = "WormaCeptor"
private const val FILE_PREFIX = "WormaCeptor_"
private const val PROVIDER_SUFFIX = ".wormaceptor.fileprovider"

private fun resolveImageMimeType(format: String): String = when (format.uppercase()) {
    "PNG" -> "image/png"
    "JPEG", "JPG" -> "image/jpeg"
    "GIF" -> "image/gif"
    "WEBP" -> "image/webp"
    "BMP" -> "image/bmp"
    else -> "image/png"
}

private fun generateFilename(format: String): String = "$FILE_PREFIX${System.currentTimeMillis()}.${format.lowercase()}"

fun saveImageToGallery(
    context: Context,
    imageData: ByteArray,
    format: String,
): ImageOperationResult {
    return try {
        val filename = generateFilename(format)
        val mimeType = resolveImageMimeType(format)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(context, imageData, filename, mimeType)
        } else {
            @Suppress("DEPRECATION")
            saveWithLegacyStorage(context, imageData, filename, mimeType)
        }
    } catch (e: Exception) {
        ImageOperationResult.Failure(context.getString(R.string.viewer_image_save_failed))
    }
}

private fun saveWithMediaStore(
    context: Context,
    imageData: ByteArray,
    filename: String,
    mimeType: String,
): ImageOperationResult {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/$WORMACEPTOR_DIR",
        )
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val uri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues,
    ) ?: return ImageOperationResult.Failure(context.getString(R.string.viewer_image_save_failed))

    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
        outputStream.write(imageData)
    }

    contentValues.clear()
    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
    context.contentResolver.update(uri, contentValues, null, null)

    return ImageOperationResult.Success(context.getString(R.string.viewer_image_saved_to_gallery))
}

@Suppress("DEPRECATION")
private fun saveWithLegacyStorage(
    context: Context,
    imageData: ByteArray,
    filename: String,
    mimeType: String,
): ImageOperationResult {
    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val wormaceptorDir = File(picturesDir, WORMACEPTOR_DIR)
    if (!wormaceptorDir.exists()) {
        wormaceptorDir.mkdirs()
    }

    val file = File(wormaceptorDir, filename)
    FileOutputStream(file).use { it.write(imageData) }

    scanFile(
        context,
        arrayOf(file.absolutePath),
        arrayOf(mimeType),
        null,
    )

    return ImageOperationResult.Success(context.getString(R.string.viewer_image_saved_to_gallery))
}

fun shareImage(
    context: Context,
    imageData: ByteArray,
    format: String,
): ImageOperationResult {
    return try {
        val filename = generateFilename(format)
        val mimeType = resolveImageMimeType(format)

        val file = File(context.cacheDir, filename)
        file.outputStream().use { it.write(imageData) }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}$PROVIDER_SUFFIX",
            file,
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.viewer_image_share_title)),
        )
        ImageOperationResult.Success(context.getString(R.string.viewer_image_share))
    } catch (e: Exception) {
        ImageOperationResult.Failure(context.getString(R.string.viewer_image_share_failed))
    }
}
