package com.atproto.identity

import kotlinx.serialization.Serializable
import morpho.app.api.Handle

@Serializable
public data class UpdateHandleRequest(
  public val handle: Handle,
)
