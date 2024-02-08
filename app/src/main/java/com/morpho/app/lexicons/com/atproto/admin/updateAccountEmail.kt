package com.atproto.admin

import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.AtIdentifier

@Serializable
public data class UpdateAccountEmailRequest(
  /**
   * The handle or DID of the repo.
   */
  public val account: AtIdentifier,
  public val email: String,
)
