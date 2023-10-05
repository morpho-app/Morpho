package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.Handle

@Serializable
public data class GetSessionResponse(
  public val handle: Handle,
  public val did: Did,
  public val email: String? = null,
)
