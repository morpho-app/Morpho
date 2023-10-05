package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class CreateInviteCodesAccountCodes(
  public val account: String,
  public val codes: ReadOnlyList<String>,
)
