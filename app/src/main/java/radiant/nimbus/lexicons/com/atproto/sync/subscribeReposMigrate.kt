package com.atproto.sync

import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.Timestamp

@Serializable
public data class SubscribeReposMigrate(
  public val seq: Long,
  public val did: Did,
  public val migrateTo: String? = null,
  public val time: Timestamp,
)
