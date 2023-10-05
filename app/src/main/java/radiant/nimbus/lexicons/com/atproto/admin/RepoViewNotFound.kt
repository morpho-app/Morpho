package com.atproto.admin

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did

@Serializable
public data class RepoViewNotFound(
  public val did: Did,
)
