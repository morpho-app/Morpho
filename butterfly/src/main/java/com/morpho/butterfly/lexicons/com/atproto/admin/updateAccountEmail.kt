package com.atproto.admin

import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtIdentifier

@Serializable
public data class UpdateAccountEmailRequest(
  /**
   * The handle or DID of the repo.
   */
  public val account: AtIdentifier,
  public val email: String,
)
