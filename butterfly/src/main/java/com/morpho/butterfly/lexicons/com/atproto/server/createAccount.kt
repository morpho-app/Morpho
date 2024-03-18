package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.Handle

@Serializable
public data class CreateAccountRequest(
  public val email: String,
  public val handle: Handle,
  public val did: Did? = null,
  public val inviteCode: String? = null,
  public val password: String,
  public val recoveryKey: String? = null,
)

@Serializable
public data class CreateAccountResponse(
  public val accessJwt: String,
  public val refreshJwt: String,
  public val handle: Handle,
  public val did: Did,
)
