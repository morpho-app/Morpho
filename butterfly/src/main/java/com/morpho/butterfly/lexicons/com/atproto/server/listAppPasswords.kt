package com.atproto.server

import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class ListAppPasswordsResponse(
  public val passwords: ReadOnlyList<ListAppPasswordsAppPassword>,
)
