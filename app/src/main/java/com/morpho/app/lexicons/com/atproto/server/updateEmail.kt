package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class UpdateEmailRequest(
  public val email: String,
  /**
   * Requires a token from com.atproto.sever.requestEmailUpdate if the account's email has been
   * confirmed.
   */
  public val token: String? = null,
)
