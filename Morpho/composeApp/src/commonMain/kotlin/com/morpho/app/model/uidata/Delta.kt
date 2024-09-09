package com.morpho.app.model.uidata

import androidx.compose.runtime.Immutable
import com.morpho.app.util.JavaSerializable
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Immutable
@Serializable
data class Delta(
    val duration: Duration,
): JavaSerializable