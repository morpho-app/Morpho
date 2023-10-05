package com.atproto.server

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.Timestamp

@Serializable
public data class InviteCodeUse(
  public val usedBy: Did,
  public val usedAt: Timestamp,
)
