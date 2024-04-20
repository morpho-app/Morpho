package com.morpho.app.model.uidata

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class Delta(
    val duration: Duration,
)