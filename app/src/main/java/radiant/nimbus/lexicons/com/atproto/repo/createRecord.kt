package com.atproto.repo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid
import radiant.nimbus.api.Nsid

@Serializable
public data class CreateRecordRequest(
  /**
   * The handle or DID of the repo.
   */
  public val repo: AtIdentifier,
  /**
   * The NSID of the record collection.
   */
  public val collection: Nsid,
  /**
   * The key of the record.
   */
  public val rkey: String? = null,
  /**
   * Validate the record?
   */
  public val validate: Boolean? = true,
  /**
   * The record to create.
   */
  public val record: JsonElement,
  /**
   * Compare and swap with the previous commit by cid.
   */
  public val swapCommit: Cid? = null,
) {
  init {
    require(rkey == null || rkey.count() <= 15) {
      "rkey.count() must be <= 15, but was ${rkey?.count()}"
    }
  }
}

@Serializable
public data class CreateRecordResponse(
  public val uri: AtUri,
  public val cid: Cid,
)