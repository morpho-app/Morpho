package com.atproto.admin

import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.model.Timestamp

@Serializable
public data class ActionReversal(
  public val reason: String,
  public val createdBy: Did,
  public val createdAt: Timestamp,
)
