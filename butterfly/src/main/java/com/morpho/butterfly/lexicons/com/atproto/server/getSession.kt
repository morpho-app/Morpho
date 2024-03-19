package com.atproto.server

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle

@Serializable
public data class GetSessionResponse(
  public val handle: Handle,
  public val did: Did,
  public val email: String? = null,
  public val emailConfirmed: Boolean? = null,
)
