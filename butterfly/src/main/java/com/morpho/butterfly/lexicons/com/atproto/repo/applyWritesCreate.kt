package com.atproto.repo

import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.Nsid

/**
 * Create a new record.
 */
@Serializable
public data class ApplyWritesCreate(
  public val collection: Nsid,
  public val rkey: String? = null,
  public val `value`: JsonElement,
) {
  init {
    require(rkey == null || rkey.count() <= 15) {
      "rkey.count() must be <= 15, but was ${rkey?.count()}"
    }
  }
}
