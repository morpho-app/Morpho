package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did

@Serializable
public data class DeleteAccountRequest(
  public val did: Did,
  public val password: String,
  public val token: String,
)
