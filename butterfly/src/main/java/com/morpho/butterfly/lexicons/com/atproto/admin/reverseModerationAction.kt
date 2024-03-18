package com.atproto.admin

import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did

@Serializable
public data class ReverseModerationActionRequest(
  public val id: Long,
  public val reason: String,
  public val createdBy: Did,
)

public typealias ReverseModerationActionResponse = ActionView
