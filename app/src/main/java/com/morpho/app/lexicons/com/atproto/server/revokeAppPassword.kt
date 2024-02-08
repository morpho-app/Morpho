package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class RevokeAppPasswordRequest(
  public val name: String,
)
