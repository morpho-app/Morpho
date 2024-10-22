package com.morpho.app.model.uidata

import androidx.compose.runtime.Immutable
import com.morpho.butterfly.json
import dev.icerock.moko.parcelize.Parcel
import dev.icerock.moko.parcelize.Parceler
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline


@Serializable
@Immutable
@JvmInline
value class Moment(
    val instant: Instant,
): Comparable<Moment> {
    operator fun plus(delta: Delta): Moment = Moment(instant + delta.duration)

    operator fun minus(delta: Delta): Moment = Moment(instant - delta.duration)

    operator fun minus(other: Moment): Delta = Delta(instant - other.instant)

    override fun compareTo(other: Moment): Int = instant.compareTo(instant)
}

object MomentParceler : Parceler<Moment>{
    override fun create(parcel: Parcel): Moment {
        val moment = parcel.readString()?.substringAfter("t")?.substringBefore("Z")
        return moment?.let { Moment(Instant.fromEpochMilliseconds(it.toLong())) }
            ?: Moment(Instant.DISTANT_PAST)
    }

    override fun Moment.write(parcel: Parcel, flags: Int) {
        parcel.writeString("t${this.instant.toEpochMilliseconds()}Z")
    }
}

object MaybeMomentParceler : Parceler<Moment?>{
    override fun create(parcel: Parcel): Moment? {
        val moment = parcel.readString()?.substringAfter("t")?.substringBefore("Z")
        if(moment == "0") return null
        return moment?.let { Moment(Instant.fromEpochMilliseconds(it.toLong())) }
    }

    override fun Moment?.write(parcel: Parcel, flags: Int) {
        if(this == null) {
            parcel.writeString("t0Z")
            return
        } else {
            parcel.writeString("t${this.instant.toEpochMilliseconds()}Z")
        }
    }
}

object JsonElementParceler : Parceler<JsonElement>{
    override fun create(parcel: Parcel): JsonElement {
        val serialized = parcel.readString()
        return serialized?.let { json.parseToJsonElement(it) } ?: JsonObject(emptyMap())
    }

    override fun JsonElement.write(parcel: Parcel, flags: Int) {
        parcel.writeString(json.encodeToString(JsonElement.serializer(), this))
    }
}

