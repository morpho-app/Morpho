package com.atproto.admin

import kotlinx.serialization.Serializable
import morpho.app.api.Did

@Serializable
public data class RepoRef(
  public val did: Did,
)
