package com.atproto.repo

import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.Nsid

/**
 * Update an existing record.
 */
@Serializable
public data class ApplyWritesUpdate(
  public val collection: Nsid,
  public val rkey: String,
  public val `value`: JsonElement,
)
