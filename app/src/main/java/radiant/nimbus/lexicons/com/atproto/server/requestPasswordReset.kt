package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class RequestPasswordResetRequest(
  public val email: String,
)
