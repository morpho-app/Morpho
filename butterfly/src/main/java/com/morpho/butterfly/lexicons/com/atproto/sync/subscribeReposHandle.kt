package com.atproto.sync

import kotlin.Long
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class SubscribeReposHandle(
  public val seq: Long,
  public val did: Did,
  public val handle: Handle,
  public val time: Timestamp,
)
