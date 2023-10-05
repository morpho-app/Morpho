package com.atproto.admin

import kotlin.Long
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class ResolveModerationReportsRequest(
  public val actionId: Long,
  public val reportIds: ReadOnlyList<Long>,
  public val createdBy: Did,
)

public typealias ResolveModerationReportsResponse = ActionView
