package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle

@Serializable
public data class RefreshSessionResponse(
  public val accessJwt: String,
  public val refreshJwt: String,
  public val handle: Handle,
  public val did: Did,
)
