package com.atproto.server

import kotlinx.serialization.Serializable
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class ListAppPasswordsResponse(
  public val passwords: ReadOnlyList<ListAppPasswordsAppPassword>,
)
