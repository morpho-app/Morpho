package com.atproto.admin

import kotlin.Long
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class ResolveModerationReportsRequest(
  public val actionId: Long,
  public val reportIds: ReadOnlyList<Long>,
  public val createdBy: Did,
)

public typealias ResolveModerationReportsResponse = ActionView
