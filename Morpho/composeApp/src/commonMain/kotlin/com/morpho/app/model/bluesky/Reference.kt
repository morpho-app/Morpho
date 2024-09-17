package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Reference(
    val uri: AtUri,
    val cid: Cid,
)