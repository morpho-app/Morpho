package com.morpho.butterfly

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

object AtIdentifierSerializer : JsonContentPolymorphicSerializer<AtIdentifier>(AtIdentifier::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AtIdentifier> {
        return if (element.jsonPrimitive.content.startsWith("did:")) {
            Did.serializer()
        } else {
            Handle.serializer()
        }
    }
}