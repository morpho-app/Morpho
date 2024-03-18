package com.atproto.admin

import kotlinx.serialization.Serializable

@Serializable
public data class Moderation(
  public val currentAction: ActionViewCurrent? = null,
)
