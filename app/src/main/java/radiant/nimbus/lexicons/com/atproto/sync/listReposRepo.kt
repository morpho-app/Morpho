package com.atproto.sync

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Cid
import radiant.nimbus.api.Did

@Serializable
public data class ListReposRepo(
  public val did: Did,
  public val head: Cid,
)
