package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class CreateInviteCodesAccountCodes(
  public val account: String,
  public val codes: ReadOnlyList<String>,
)
