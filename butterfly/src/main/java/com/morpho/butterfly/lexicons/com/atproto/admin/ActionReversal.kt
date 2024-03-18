package com.atproto.admin

import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class ActionReversal(
  public val reason: String,
  public val createdBy: Did,
  public val createdAt: Timestamp,
)
