package com.atproto.admin

import kotlin.String
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.Timestamp

@Serializable
public data class ActionReversal(
  public val reason: String,
  public val createdBy: Did,
  public val createdAt: Timestamp,
)
