package com.morpho.app.model.bluesky

import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid

@Serializable
data class Reference(
    val uri: AtUri,
    val cid: Cid,
)