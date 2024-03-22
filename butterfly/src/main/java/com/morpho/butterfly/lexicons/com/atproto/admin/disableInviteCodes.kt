package com.atproto.admin

import kotlin.String
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class DisableInviteCodesRequest(
  public val codes: ReadOnlyList<String> = persistentListOf(),
  public val accounts: ReadOnlyList<String> = persistentListOf(),
)
