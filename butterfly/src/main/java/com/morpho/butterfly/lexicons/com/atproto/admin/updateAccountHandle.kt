package com.atproto.admin

import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle

@Serializable
public data class UpdateAccountHandleRequest(
  public val did: Did,
  public val handle: Handle,
)
