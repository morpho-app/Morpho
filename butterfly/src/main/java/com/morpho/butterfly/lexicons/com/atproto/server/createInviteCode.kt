package com.atproto.server

import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did

@Serializable
public data class CreateInviteCodeRequest(
  public val useCount: Long,
  public val forAccount: Did? = null,
)

@Serializable
public data class CreateInviteCodeResponse(
  public val code: String,
)
