package com.morpho.butterfly.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@SerialName("blob")
@Serializable(with = BlobSerializer::class)
sealed interface Blob {

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class StandardBlob(
        @ByteString val ref: BlobRef,
        val mimeType: String,
        val size: Long,
    ) : Blob

    @Serializable
    data class LegacyBlob(
        val cid: String,
        val mimeType: String,
    ) : Blob
}

@Serializable
data class BlobRef(
    @SerialName("\$link")
    val link: String,
)

class BlobSerializer : JsonContentPolymorphicSerializer<Blob>(Blob::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Blob> {
        return if (element.jsonObject.containsKey("ref")) {
            Blob.StandardBlob.serializer()
        } else {
            Blob.LegacyBlob.serializer()
        }
    }
}