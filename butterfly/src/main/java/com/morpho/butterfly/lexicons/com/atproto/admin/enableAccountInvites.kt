package com.atproto.admin

import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.Did

@Serializable
public data class EnableAccountInvitesRequest(
  public val account: Did,
  /**
   * Additionally add a note describing why the invites were enabled
   */
  public val note: String? = null,
)
