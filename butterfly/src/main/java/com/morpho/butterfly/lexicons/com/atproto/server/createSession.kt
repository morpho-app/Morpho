package com.atproto.server

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle

@Serializable
public data class CreateSessionRequest(
  /**
   * Handle or other identifier supported by the server for the authenticating user.
   */
  public val identifier: String,
  public val password: String,
)

@Serializable
public data class CreateSessionResponse(
  public val accessJwt: String,
  public val refreshJwt: String,
  public val handle: Handle,
  public val did: Did,
  public val email: String? = null,
  public val emailConfirmed: Boolean? = null,
)
