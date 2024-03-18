package com.atproto.sync

import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class SubscribeReposMigrate(
  public val seq: Long,
  public val did: Did,
  public val migrateTo: String? = null,
  public val time: Timestamp,
)
