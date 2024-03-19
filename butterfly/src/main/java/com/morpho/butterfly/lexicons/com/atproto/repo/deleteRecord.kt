package com.atproto.repo

import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Nsid

@Serializable
public data class DeleteRecordRequest(
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
  public val rkey: String,
  /**
   * Compare and swap with the previous record by cid.
   */
  public val swapRecord: Cid? = null,
  /**
   * Compare and swap with the previous commit by cid.
   */
  public val swapCommit: Cid? = null,
)
