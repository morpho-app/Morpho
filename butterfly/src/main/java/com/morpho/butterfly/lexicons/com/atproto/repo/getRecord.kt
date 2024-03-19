package com.atproto.repo

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Nsid
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetRecordQuery(
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
   * The CID of the version of the record. If not specified, then return the most recent version.
   */
  public val cid: Cid? = null,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("repo" to repo)
    add("collection" to collection)
    add("rkey" to rkey)
    add("cid" to cid)
  }.toImmutableList()
}

@Serializable
public data class GetRecordResponse(
  public val uri: AtUri,
  public val cid: Cid? = null,
  public val `value`: JsonElement,
)
