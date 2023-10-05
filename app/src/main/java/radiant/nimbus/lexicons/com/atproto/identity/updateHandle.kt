package com.atproto.identity

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Handle

@Serializable
public data class UpdateHandleRequest(
  public val handle: Handle,
)
