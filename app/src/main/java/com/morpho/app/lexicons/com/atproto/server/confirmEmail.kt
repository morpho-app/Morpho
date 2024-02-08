package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class ConfirmEmailRequest(
  public val email: String,
  public val token: String,
)
