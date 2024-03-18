package com.atproto.sync

import kotlin.Long
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class SubscribeReposTombstone(
  public val seq: Long,
  public val did: Did,
  public val time: Timestamp,
)
