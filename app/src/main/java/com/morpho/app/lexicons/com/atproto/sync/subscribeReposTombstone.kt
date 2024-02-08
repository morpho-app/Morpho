package com.atproto.sync

import kotlin.Long
import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.model.Timestamp

@Serializable
public data class SubscribeReposTombstone(
  public val seq: Long,
  public val did: Did,
  public val time: Timestamp,
)
