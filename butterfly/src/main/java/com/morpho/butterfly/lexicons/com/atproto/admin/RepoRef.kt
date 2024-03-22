package com.atproto.admin

import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did

@Serializable
public data class RepoRef(
  public val did: Did,
)
