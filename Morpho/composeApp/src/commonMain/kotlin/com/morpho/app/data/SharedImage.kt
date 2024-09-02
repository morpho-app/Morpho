package com.morpho.app.data

import androidx.compose.ui.graphics.ImageBitmap
import app.bsky.embed.AspectRatio
import com.morpho.app.util.deserialize
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.model.Blob
import io.github.vinceglb.filekit.core.PlatformFile
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import okio.ByteString.Companion.decodeBase64


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class SharedImage {
    val mimeType: String
    companion object {
        fun fromByteArray(byteArray: ByteArray): SharedImage
    }
    fun toByteArray(): ByteArray?
    fun toImageBitmap(): ImageBitmap?
    fun getAspectRatio(): AspectRatio?
}

expect suspend fun PlatformFile.toSharedImage(): SharedImage


class SharedImageSerializer @OptIn(ExperimentalSerializationApi::class)
constructor(override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
    "com.morpho.app.data.SharedImage",
    PrimitiveKind.STRING
)
) : KSerializer<SharedImage> {

    override fun deserialize(decoder: Decoder): SharedImage {
        val bytes = decoder.decodeString().decodeBase64()?.toByteArray()
            ?: throw IllegalArgumentException("Failed to decode base64 string")
        return SharedImage.fromByteArray(bytes)
    }


    override fun serialize(encoder: Encoder, value: SharedImage) {
        val byteArray = value.toByteArray() ?: return
        val base64 = byteArray.encodeBase64()
        encoder.encodeString(base64)
    }

}

suspend fun imageToBlob(image: SharedImage, api: Butterfly): Blob? {
    val byteArray = image.toByteArray() ?: return null
    val base64 = byteArray.encodeBase64().toByteArray()
    val resp = api.api.uploadBlob(base64, image.mimeType).getOrNull() ?: return null
    return when (val blob = Blob.serializer().deserialize(resp.blob)) {
        is Blob.StandardBlob -> Blob.StandardBlob(blob.ref, image.mimeType, blob.size)
        is Blob.LegacyBlob -> Blob.LegacyBlob(blob.cid, image.mimeType)
    }
}

fun fileExtToMimeType(filename: String): String {
    val ext = filename.substringAfterLast(".", "")
    return when (ext) {
        "png" -> "image/png"
        "jpg" -> "image/jpeg"
        "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        else -> "image/*"
    }
}