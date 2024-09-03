package com.morpho.app.data

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import app.bsky.embed.AspectRatio
import io.github.vinceglb.filekit.core.PlatformFile
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SharedImage(private val bitmap: android.graphics.Bitmap?, actual val mimeType: String) {
    actual fun toByteArray(): ByteArray? {
        return if (bitmap != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            when (mimeType) {
                "image/png" -> bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                "image/jpeg" -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                "image/webp" -> bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 100, byteArrayOutputStream)
                else -> bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100, byteArrayOutputStream)
            }

            byteArrayOutputStream.toByteArray()
        } else {
            println("toByteArray null")
            null
        }
    }

    actual fun toByteArray(targetSize: Long): ByteArray? {
        return if (bitmap != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            when(mimeType) {
                "image/png" -> bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 70, byteArrayOutputStream)
                "image/jpeg" -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
                "image/webp" -> bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 70, byteArrayOutputStream)
                else -> bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,70, byteArrayOutputStream)
            }
            if (byteArrayOutputStream.size() > targetSize) {
                val scale = (sqrt(targetSize.toDouble() / byteArrayOutputStream.size()) * 40.0).toInt()
                byteArrayOutputStream.reset()
                when(mimeType) {
                    "image/png" -> bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, scale, byteArrayOutputStream)
                    "image/jpeg" -> bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, scale, byteArrayOutputStream)
                    "image/webp" -> bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, scale, byteArrayOutputStream)
                    else -> bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,scale, byteArrayOutputStream)
                }

            }
            byteArrayOutputStream.toByteArray()
        } else {
            println("toByteArray null")
            null
        }
    }

    actual fun toImageBitmap(): ImageBitmap? {
        val byteArray = toByteArray()
        return if (byteArray != null) {
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
        } else {
            println("toImageBitmap null")
            null
        }
    }

    actual fun getAspectRatio(): AspectRatio? {
        val width = bitmap?.width
        val height = bitmap?.height
        return if (width != null && height != null) {
            AspectRatio(width.toLong(), height.toLong())
        } else {
            null
        }
    }

    actual companion object {
        actual fun fromByteArray(byteArray: ByteArray): SharedImage {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            return SharedImage(bitmap, "image/*")
        }
    }
}

actual suspend fun PlatformFile.toSharedImage(): SharedImage {
    val bytes = this.readBytes()
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return SharedImage(bitmap, fileExtToMimeType(this.name))
}
