package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.model.Timestamp

@Serializable
public data class CreateAppPasswordAppPassword(
  public val name: String,
  public val password: String,
  public val createdAt: Timestamp,
)
