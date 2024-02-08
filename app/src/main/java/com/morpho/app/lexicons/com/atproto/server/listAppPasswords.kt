package com.atproto.server

import kotlinx.serialization.Serializable
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class ListAppPasswordsResponse(
  public val passwords: ReadOnlyList<ListAppPasswordsAppPassword>,
)
