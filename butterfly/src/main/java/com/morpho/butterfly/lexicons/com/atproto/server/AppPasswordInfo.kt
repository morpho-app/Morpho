package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class AppPasswordInfo(
  public val name: String,
  public val password: String,
  public val createdAt: Timestamp,
)
