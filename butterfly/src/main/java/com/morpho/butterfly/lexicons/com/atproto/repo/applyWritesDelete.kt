package com.atproto.repo

import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Nsid

/**
 * Delete an existing record.
 */
@Serializable
public data class ApplyWritesDelete(
  public val collection: Nsid,
  public val rkey: String,
)
