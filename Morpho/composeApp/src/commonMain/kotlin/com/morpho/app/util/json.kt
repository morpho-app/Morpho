package com.morpho.app.util

import app.bsky.actor.PreferencesUnion
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer

@OptIn(InternalSerializationApi::class)
val morphoSerializersModule = SerializersModule {
    polymorphic(PreferencesUnion::class) {
        subclass(PreferencesUnion.AdultContentPref::class)
        subclass(PreferencesUnion.FeedViewPref::class)
        subclass(PreferencesUnion.ThreadViewPref::class)
        subclass(PreferencesUnion.SkyFeedBuilderFeedsPref::class)
        subclass(PreferencesUnion.SavedFeedsPref::class)
        subclass(PreferencesUnion.SavedFeedsPrefV2::class)
        subclass(PreferencesUnion.PersonalDetailsPref::class)
        subclass(PreferencesUnion.ContentLabelPref::class)
        subclass(PreferencesUnion.LabelersPref::class)
        subclass(PreferencesUnion.HiddenPostsPref::class)
        subclass(PreferencesUnion.MutedWordsPref::class)
        subclass(PreferencesUnion.InterestsPref::class)
        subclass(PreferencesUnion.ButterflyPreference::class)
        subclass(PreferencesUnion.UnknownPreference::class)
        defaultDeserializer { _ ->
            PreferencesUnion.UnknownPreference::class.serializer()
        }
    }
}


val json = Json {
    classDiscriminator = "${'$'}type"
    ignoreUnknownKeys = true
    prettyPrint = true
    serializersModule = morphoSerializersModule
}

val JsonElement.recordType: String
    get() = jsonObject[json.configuration.classDiscriminator]!!.jsonPrimitive.content

fun <T : Any> KSerializer<T>.deserialize(jsonElement: JsonElement): T {
    return json.decodeFromString(this, json.encodeToString(jsonElement))
}

fun <T : Any> KSerializer<T>.deserialize(string: String): T {
    return json.decodeFromString(this, string)
}

fun <T : Any> KSerializer<T>.serialize(value: T): JsonElement {
    return json.parseToJsonElement(json.encodeToString(this, value))
}

