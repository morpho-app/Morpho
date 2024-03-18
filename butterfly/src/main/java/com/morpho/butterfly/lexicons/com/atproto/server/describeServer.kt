package com.atproto.server

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class DescribeServerResponse(
  public val inviteCodeRequired: Boolean? = null,
  public val availableUserDomains: ReadOnlyList<String>,
  public val links: DescribeServerLinks? = null,
)
