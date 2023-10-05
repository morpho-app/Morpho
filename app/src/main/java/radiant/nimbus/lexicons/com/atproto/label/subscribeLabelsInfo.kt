package com.atproto.label

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class SubscribeLabelsInfo(
  public val name: SubscribeLabelsName,
  public val message: String? = null,
)
