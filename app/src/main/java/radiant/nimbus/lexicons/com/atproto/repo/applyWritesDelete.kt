package com.atproto.repo

import kotlin.String
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Nsid

/**
 * Delete an existing record.
 */
@Serializable
public data class ApplyWritesDelete(
  public val collection: Nsid,
  public val rkey: String,
)
