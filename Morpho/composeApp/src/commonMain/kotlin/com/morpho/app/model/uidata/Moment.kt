package com.morpho.app.model.uidata

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@Immutable
@JvmInline
value class Moment(
    val instant: Instant,
) : Comparable<Moment> {
    operator fun plus(delta: Delta): Moment = Moment(instant + delta.duration)

    operator fun minus(delta: Delta): Moment = Moment(instant - delta.duration)

    operator fun minus(other: Moment): Delta = Delta(instant - other.instant)

    override fun compareTo(other: Moment): Int = instant.compareTo(instant)
}
