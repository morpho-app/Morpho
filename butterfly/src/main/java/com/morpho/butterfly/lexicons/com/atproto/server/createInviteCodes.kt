package com.atproto.server

import kotlin.Long
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class CreateInviteCodesRequest(
  public val codeCount: Long,
  public val useCount: Long,
  public val forAccounts: ReadOnlyList<Did> = persistentListOf(),
)

@Serializable
public data class CreateInviteCodesResponse(
  public val codes: ReadOnlyList<CreateInviteCodesAccountCodes>,
)
