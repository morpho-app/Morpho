package com.atproto.repo

import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid

@Serializable
public data class StrongRef(
  public val uri: AtUri,
  public val cid: Cid,
)
