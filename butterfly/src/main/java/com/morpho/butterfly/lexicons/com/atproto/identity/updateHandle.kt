package com.atproto.identity

import kotlinx.serialization.Serializable
import com.morpho.butterfly.Handle

@Serializable
public data class UpdateHandleRequest(
  public val handle: Handle,
)
