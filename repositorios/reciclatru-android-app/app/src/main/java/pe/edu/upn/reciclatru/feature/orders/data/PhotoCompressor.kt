package pe.edu.upn.reciclatru.feature.orders.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class PhotoCompressor @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun compress(photo: String): ByteArray {
        val uri = Uri.parse(photo)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("No se pudo leer la foto")

        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MaxSidePx) sampleSize *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = open(uri).use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IOException("No se pudo leer la foto")

        val bitmap = decoded.scaledAndRotated(rotationOf(uri))
        var quality = InitialQuality
        var bytes = bitmap.toJpeg(quality)
        while (bytes.size > TargetBytes && quality > MinQuality) {
            quality -= QualityStep
            bytes = bitmap.toJpeg(quality)
        }
        return bytes
    }

    private fun open(uri: Uri) =
        context.contentResolver.openInputStream(uri) ?: throw IOException("No se pudo abrir la foto")

    private fun rotationOf(uri: Uri): Float = runCatching {
        open(uri).use { stream ->
            when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }
    }.getOrDefault(0f)

    private fun Bitmap.scaledAndRotated(rotation: Float): Bitmap {
        val longest = max(width, height)
        val scale = if (longest > MaxSidePx) MaxSidePx.toFloat() / longest else 1f
        if (scale == 1f && rotation == 0f) return this
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postRotate(rotation)
        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun Bitmap.toJpeg(quality: Int): ByteArray =
        ByteArrayOutputStream().also { compress(Bitmap.CompressFormat.JPEG, quality, it) }.toByteArray()

    private companion object {
        const val MaxSidePx = 1280
        const val TargetBytes = 300 * 1024
        const val InitialQuality = 85
        const val MinQuality = 35
        const val QualityStep = 10
    }
}
