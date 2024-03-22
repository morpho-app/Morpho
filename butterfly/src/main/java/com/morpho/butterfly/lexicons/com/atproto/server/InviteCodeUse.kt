package com.atproto.server

import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class InviteCodeUse(
  public val usedBy: Did,
  public val usedAt: Timestamp,
)
