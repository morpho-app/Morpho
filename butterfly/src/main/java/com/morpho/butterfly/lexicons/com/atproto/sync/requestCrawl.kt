package com.atproto.sync

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class RequestCrawlRequest(
  /**
   * Hostname of the service that is requesting to be crawled.
   */
  public val hostname: String,
)
