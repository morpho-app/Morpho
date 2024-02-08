package com.atproto.admin

import kotlin.Long
import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class ResolveModerationReportsRequest(
  public val actionId: Long,
  public val reportIds: ReadOnlyList<Long>,
  public val createdBy: Did,
)

public typealias ResolveModerationReportsResponse = ActionView
