package com.atproto.sync

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class SubscribeReposInfo(
  public val name: SubscribeReposName,
  public val message: String? = null,
)
