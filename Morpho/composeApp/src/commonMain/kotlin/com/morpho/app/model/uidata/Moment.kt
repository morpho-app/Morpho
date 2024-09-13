package com.morpho.app.model.uidata

import androidx.compose.runtime.Immutable
import com.morpho.app.model.bluesky.BskyPostThread
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uistate.ContentCardState.ProfileTimeline
import com.morpho.butterfly.json
import dev.icerock.moko.parcelize.Parcel
import dev.icerock.moko.parcelize.Parceler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

object AtCursorMutableSharedFlowParceler : Parceler<MutableSharedFlow<AtCursor>>{
    override fun create(parcel: Parcel): MutableSharedFlow<AtCursor> {
        val serialized = parcel.readString()
        val flow = initAtCursor()
        if (serialized != null) {
            json.decodeFromString(AtCursor.serializer(), serialized).let { cursor ->
                flow.tryEmit(cursor)
            }
        }
        return flow
    }

    override fun MutableSharedFlow<AtCursor>.write(parcel: Parcel, flags: Int) {
        val serialized = json.encodeToString(AtCursor.serializer(), this.replayCache.lastOrNull() ?: AtCursor.EMPTY)
        parcel.writeString(serialized)
    }
}

object PostThreadStateFlowParceler : Parceler<StateFlow<BskyPostThread?>>{
    override fun create(parcel: Parcel): StateFlow<BskyPostThread?> {
        val serialized = parcel.readString()
        val flow = MutableStateFlow(null)
        return flow.asStateFlow()
    }

    override fun StateFlow<BskyPostThread?>.write(parcel: Parcel, flags: Int) {
        if(this.value == null) {
            parcel.writeString("null")
            return
        }
        val serialized = json.encodeToString(BskyPostThread.serializer(), this.value!!)
        parcel.writeString(serialized)
    }
}

object ProfileTimelineStateFlowParceler : Parceler<StateFlow<ProfileTimeline<MorphoDataItem.FeedItem>?>>{
    override fun create(parcel: Parcel): StateFlow<ProfileTimeline<MorphoDataItem.FeedItem>?> {
        val serialized = parcel.readString()
        val flow = MutableStateFlow(null)
        return flow.asStateFlow()
    }

    override fun StateFlow<ProfileTimeline<MorphoDataItem.FeedItem>?>.write(parcel: Parcel, flags: Int) {
        if(this.value == null) {
            parcel.writeString("null")
            return
        }

        parcel.writeString("${this.value!!.uri}")
    }
}

object ProfileListsStateFlowParceler : Parceler<StateFlow<ProfileTimeline<MorphoDataItem.ListInfo>?>>{
    override fun create(parcel: Parcel): StateFlow<ProfileTimeline<MorphoDataItem.ListInfo>?> {
        val serialized = parcel.readString()
        val flow = MutableStateFlow(null)
        return flow.asStateFlow()
    }

    override fun StateFlow<ProfileTimeline<MorphoDataItem.ListInfo>?>.write(parcel: Parcel, flags: Int) {
        if(this.value == null) {
            parcel.writeString("null")
            return
        }

        parcel.writeString("${this.value!!.uri}")
    }
}

object ProfileFeedsStateFlowParceler : Parceler<StateFlow<ProfileTimeline<MorphoDataItem.FeedInfo>?>>{
    override fun create(parcel: Parcel): StateFlow<ProfileTimeline<MorphoDataItem.FeedInfo>?> {
        val serialized = parcel.readString()
        val flow = MutableStateFlow(null)
        return flow.asStateFlow()
    }

    override fun StateFlow<ProfileTimeline<MorphoDataItem.FeedInfo>?>.write(parcel: Parcel, flags: Int) {
        if(this.value == null) {
            parcel.writeString("null")
            return
        }

        parcel.writeString("${this.value!!.uri}")
    }
}

object ProfileLabelServiceStateFlowParceler : Parceler<StateFlow<ProfileTimeline<MorphoDataItem.LabelService>?>>{
    override fun create(parcel: Parcel): StateFlow<ProfileTimeline<MorphoDataItem.LabelService>?> {
        val serialized = parcel.readString()
        val flow = MutableStateFlow(null)
        return flow.asStateFlow()
    }

    override fun StateFlow<ProfileTimeline<MorphoDataItem.LabelService>?>.write(parcel: Parcel, flags: Int) {
        if(this.value == null) {
            parcel.writeString("null")
            return
        }

        parcel.writeString("${this.value!!.uri}")
    }
}