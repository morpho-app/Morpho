package com.atproto.admin

import kotlin.Long
import kotlinx.serialization.Serializable

@Serializable
public data class VideoDetails(
  public val width: Long,
  public val height: Long,
  public val length: Long,
)
