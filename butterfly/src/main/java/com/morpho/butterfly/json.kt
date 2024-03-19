package com.morpho.butterfly

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

val json = Json {
    classDiscriminator = "${'$'}type"
    ignoreUnknownKeys = true
    prettyPrint = true
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

fun getRkey(uri: AtUri?) : String {
    val str = uri?.atUri.orEmpty()
    return str.substringAfterLast("/")
}