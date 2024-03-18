package com.atproto.admin

import kotlin.Long
import kotlinx.serialization.Serializable

@Serializable
public data class ImageDetails(
  public val width: Long,
  public val height: Long,
)
