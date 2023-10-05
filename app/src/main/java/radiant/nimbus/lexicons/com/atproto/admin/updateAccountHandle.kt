package com.atproto.admin

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.Handle

@Serializable
public data class UpdateAccountHandleRequest(
  public val did: Did,
  public val handle: Handle,
)
