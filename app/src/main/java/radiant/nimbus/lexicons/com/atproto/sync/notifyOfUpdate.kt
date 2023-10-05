package com.atproto.sync

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class NotifyOfUpdateRequest(
  /**
   * Hostname of the service that is notifying of update.
   */
  public val hostname: String,
)
