package com.atproto.admin

import kotlin.Long
import kotlinx.serialization.Serializable

@Serializable
public data class ActionViewCurrent(
  public val id: Long,
  public val action: Token,
  /**
   * Indicates how long this action was meant to be in effect before automatically expiring.
   */
  public val durationInHours: Long? = null,
)
