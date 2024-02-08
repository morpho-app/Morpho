package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.Handle

@Serializable
public data class RefreshSessionResponse(
  public val accessJwt: String,
  public val refreshJwt: String,
  public val handle: Handle,
  public val did: Did,
)
