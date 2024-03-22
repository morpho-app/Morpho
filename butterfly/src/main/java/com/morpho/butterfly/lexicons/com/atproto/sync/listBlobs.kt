package com.atproto.sync

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class ListBlobsQuery(
  /**
   * The DID of the repo.
   */
  public val did: Did,
  /**
   * Optional revision of the repo to list blobs since
   */
  public val since: String? = null,
  public val limit: Long? = 500,
  public val cursor: String? = null,
) {
  init {
    require(limit == null || limit >= 1) {
      "limit must be >= 1, but was $limit"
    }
    require(limit == null || limit <= 1_000) {
      "limit must be <= 1_000, but was $limit"
    }
  }

  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("did" to did)
    add("since" to since)
    add("limit" to limit)
    add("cursor" to cursor)
  }.toImmutableList()
}

@Serializable
public data class ListBlobsResponse(
  public val cursor: String? = null,
  public val cids: ReadOnlyList<Cid>,
)
