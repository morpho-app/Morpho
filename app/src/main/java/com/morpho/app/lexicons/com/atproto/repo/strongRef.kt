package com.atproto.repo

import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.Cid

@Serializable
public data class StrongRef(
  public val uri: AtUri,
  public val cid: Cid,
)
