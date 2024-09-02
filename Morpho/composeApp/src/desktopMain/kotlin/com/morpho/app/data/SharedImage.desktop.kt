package com.morpho.app.data


import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import app.bsky.embed.AspectRatio
import io.github.vinceglb.filekit.core.PlatformFile
import org.jetbrains.skia.Image


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SharedImage(private val image: Image?, actual val mimeType: String) {
    actual fun toByteArray(): ByteArray? {
        return if (image != null) {
            image.encodeToData()?.bytes
        } else {
            println("toByteArray null")
            null
        }
    }

    actual fun toImageBitmap(): ImageBitmap? {
        val byteArray = toByteArray()
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

