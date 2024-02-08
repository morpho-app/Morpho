package com.atproto.admin

import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.Handle

@Serializable
public data class UpdateAccountHandleRequest(
  public val did: Did,
  public val handle: Handle,
)
