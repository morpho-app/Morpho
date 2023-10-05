package com.atproto.sync

import kotlin.Long
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.Handle
import radiant.nimbus.api.model.Timestamp

@Serializable
public data class SubscribeReposHandle(
  public val seq: Long,
  public val did: Did,
  public val handle: Handle,
  public val time: Timestamp,
)
