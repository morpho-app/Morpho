package com.atproto.sync

import kotlin.Long
import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.Handle
import morpho.app.api.model.Timestamp

@Serializable
public data class SubscribeReposHandle(
  public val seq: Long,
  public val did: Did,
  public val handle: Handle,
  public val time: Timestamp,
)
