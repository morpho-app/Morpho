package com.atproto.admin

import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.Did

@Serializable
public data class DisableAccountInvitesRequest(
  public val account: Did,
  /**
   * Additionally add a note describing why the invites were disabled
   */
  public val note: String? = null,
)
