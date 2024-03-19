package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class ResetPasswordRequest(
  public val token: String,
  public val password: String,
)
