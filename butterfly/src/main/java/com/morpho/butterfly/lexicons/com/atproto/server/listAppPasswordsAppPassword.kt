package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.model.Timestamp

@Serializable
public data class ListAppPasswordsAppPassword(
  public val name: String,
  public val createdAt: Timestamp,
)
