package com.morpho.app.data


import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import app.bsky.embed.AspectRatio
import io.github.vinceglb.filekit.core.PlatformFile
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import kotlin.math.sqrt


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SharedImage(private val image: Image?, actual val mimeType: String) {
    actual fun toByteArray(): ByteArray? {
        return if (image != null) {
            when(mimeType) {
                "image/png" -> image.encodeToData()?.bytes
                "image/jpeg" -> image.encodeToData(EncodedImageFormat.JPEG)?.bytes
                "image/webp" -> image.encodeToData(EncodedImageFormat.WEBP)?.bytes
                else -> {
                    println("Unsupported mimeType: $mimeType")
                    null
                }
            }
        } else {
            println("toByteArray null")
            null
        }
    }
    actual fun toByteArray(targetSize: Long): ByteArray? {
        return if (image != null) {
            val encoded = when(mimeType) {
                "image/png" -> image.encodeToData(quality = 70)?.bytes
                "image/jpeg" -> image.encodeToData(EncodedImageFormat.JPEG, 70)?.bytes
                "image/webp" -> image.encodeToData(EncodedImageFormat.WEBP, 70)?.bytes
                else -> image.encodeToData(quality = 70)?.bytes
            }
            if (encoded != null && encoded.size > targetSize) {
                val scale = (sqrt(targetSize.toDouble() / encoded.size) * 40.0).toInt()
                return when(mimeType) {
                    "image/png" -> image.encodeToData(quality = scale)?.bytes
                    "image/jpeg" -> image.encodeToData(EncodedImageFormat.JPEG, scale)?.bytes
                    "image/webp" -> image.encodeToData(EncodedImageFormat.WEBP, scale)?.bytes
                    else -> image.encodeToData(quality = scale)?.bytes
                }
            } else {
                encoded
            }
        } else {
            println("toByteArray null")
            null
        }
    }

    actual fun toImageBitmap(): ImageBitmap? {
        val byteArray = this.toByteArray()
        return if (byteArray != null) {
            return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
        } else {
            println("toImageBitmap null")
            null
        }
    }
    actual fun getAspectRatio(): AspectRatio? {
        val width = image?.width
        val height = image?.height
        return if (width != null && height != null) {
            AspectRatio(width.toLong(), height.toLong())
        } else {
            null
        }
    }

    actual companion object {
        actual fun fromByteArray(byteArray: ByteArray): SharedImage {
            val image = Image.makeFromEncoded(byteArray)
            return SharedImage(image, "image/*")
        }
    }

}

actual suspend fun PlatformFile.toSharedImage(): SharedImage {
    val bytes = file.readBytes()
    val image = Image.makeFromEncoded(bytes)
    return SharedImage(image, fileExtToMimeType(file.name))
}

