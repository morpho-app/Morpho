package com.morpho.app.data


import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import app.bsky.embed.AspectRatio
import io.github.vinceglb.filekit.core.PlatformFile
import org.jetbrains.skia.*
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
            val dimensions: Pair<Int, Int> = if (image.height > MAX_DIMENSION && image.width < MAX_DIMENSION) {
                Pair(image.width, MAX_DIMENSION)
            } else if (image.width > MAX_DIMENSION && image.height < MAX_DIMENSION) {
                Pair(MAX_DIMENSION, image.height)
            } else if (image.width > MAX_DIMENSION && image.height > MAX_DIMENSION) {
                if (image.width > image.height) {
                    Pair(MAX_DIMENSION, (MAX_DIMENSION * image.height / image.width).toInt())
                } else {
                    Pair((MAX_DIMENSION * image.width / image.height).toInt(), MAX_DIMENSION)
                }
            } else {
                Pair(image.width, image.height)
            }
            val resized = if (dimensions.first > MAX_DIMENSION || dimensions.second > MAX_DIMENSION) {
                val pixels = Pixmap.make(
                    image.imageInfo.withWidthHeight(dimensions.first, dimensions.second),
                    Data.makeUninitialized(dimensions.first * dimensions.second * image.bytesPerPixel),
                    dimensions.first * image.bytesPerPixel
                )
                image.scalePixels(pixels, SamplingMode.CATMULL_ROM, true)
                Image.makeFromPixmap(pixels)
            } else {
                image
            }
            val encoded = when(mimeType) {
                "image/png" -> resized.encodeToData(quality = 70)?.bytes
                "image/jpeg" -> resized.encodeToData(EncodedImageFormat.JPEG, 70)?.bytes
                "image/webp" -> resized.encodeToData(EncodedImageFormat.WEBP, 70)?.bytes
                else -> resized.encodeToData(quality = 70)?.bytes
            }
            if (encoded != null && encoded.size > targetSize) {
                val scale = (sqrt(targetSize.toDouble() / encoded.size) * 40.0).toInt()
                return when(mimeType) {
                    "image/png" -> resized.encodeToData(quality = scale)?.bytes
                    "image/jpeg" -> resized.encodeToData(EncodedImageFormat.JPEG, scale)?.bytes
                    "image/webp" -> resized.encodeToData(EncodedImageFormat.WEBP, scale)?.bytes
                    else -> resized.encodeToData(quality = scale)?.bytes
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
            val dimensions: Pair<Int, Int> = if (MAX_DIMENSION in (width + 1)..<height) {
                Pair(width, MAX_DIMENSION)
            } else if (MAX_DIMENSION in (height + 1)..<width) {
                Pair(MAX_DIMENSION, height)
            } else if (width > MAX_DIMENSION && height > MAX_DIMENSION) {
                if (width > height) {
                    Pair(MAX_DIMENSION, (MAX_DIMENSION * height / width))
                } else {
                    Pair((MAX_DIMENSION * width / height), MAX_DIMENSION)
                }
            } else {
                Pair(width, height)
            }
            AspectRatio(dimensions.first.toLong(), dimensions.second.toLong())
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

