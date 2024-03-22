package com.atproto.server

import kotlin.Boolean
import kotlinx.serialization.Serializable

@Serializable
public data class RequestEmailUpdateResponse(
  public val tokenRequired: Boolean,
)
