package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did

@Serializable
public data class DeleteAccountRequest(
  public val did: Did,
  public val password: String,
  public val token: String,
)
