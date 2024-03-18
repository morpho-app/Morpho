package com.atproto.server

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.model.ReadOnlyList
import morpho.app.api.model.Timestamp

@Serializable
public data class InviteCode(
  public val code: String,
  public val available: Long,
  public val disabled: Boolean,
  public val forAccount: String,
  public val createdBy: String,
  public val createdAt: Timestamp,
  public val uses: ReadOnlyList<InviteCodeUse>,
)
