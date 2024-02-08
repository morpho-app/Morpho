package com.atproto.sync

import kotlinx.serialization.Serializable
import morpho.app.api.Cid
import morpho.app.api.Did

@Serializable
public data class ListReposRepo(
  public val did: Did,
  public val head: Cid,
)
