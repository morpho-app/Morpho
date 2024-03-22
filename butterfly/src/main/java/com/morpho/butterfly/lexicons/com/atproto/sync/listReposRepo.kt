package com.atproto.sync

import kotlinx.serialization.Serializable
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did

@Serializable
public data class ListReposRepo(
  public val did: Did,
  public val head: Cid,
)
