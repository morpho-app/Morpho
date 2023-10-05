package com.atproto.admin

import kotlin.String
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtIdentifier

@Serializable
public data class UpdateAccountEmailRequest(
  /**
   * The handle or DID of the repo.
   */
  public val account: AtIdentifier,
  public val email: String,
)
