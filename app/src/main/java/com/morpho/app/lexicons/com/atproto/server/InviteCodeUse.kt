package com.atproto.server

import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.model.Timestamp

@Serializable
public data class InviteCodeUse(
  public val usedBy: Did,
  public val usedAt: Timestamp,
)
