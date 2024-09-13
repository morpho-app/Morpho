package com.morpho.app.model.uidata

import androidx.compose.runtime.Immutable
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Parcelize
@Immutable
@Serializable
data class Delta(
    val duration: Duration,
): Parcelable