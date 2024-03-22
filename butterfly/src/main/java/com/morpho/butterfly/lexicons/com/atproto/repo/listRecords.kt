package com.atproto.repo

import kotlin.Any
import kotlin.Boolean
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.Nsid
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class ListRecordsQuery(
  /**
   * The handle or DID of the repo.
   */
  public val repo: AtIdentifier,
  /**
   * The NSID of the record type.
   */
  public val collection: Nsid,
  /**
   * The number of records to return.
   */
  public val limit: Long? = 50,
  public val cursor: String? = null,
  /**
   * DEPRECATED: The lowest sort-ordered rkey to start from (exclusive)
   */
  public val rkeyStart: String? = null,
  /**
   * DEPRECATED: The highest sort-ordered rkey to stop at (exclusive)
   */
  public val rkeyEnd: String? = null,
  /**
   * Reverse the order of the returned records?
   */
  public val reverse: Boolean? = null,
) {
  init {
    require(limit == null || limit >= 1) {
      "limit must be >= 1, but was $limit"
    }
    require(limit == null || limit <= 100) {
      "limit must be <= 100, but was $limit"
    }
  }

  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("repo" to repo)
    add("collection" to collection)
    add("limit" to limit)
    add("cursor" to cursor)
    add("rkeyStart" to rkeyStart)
    add("rkeyEnd" to rkeyEnd)
    add("reverse" to reverse)
  }.toImmutableList()
}

@Serializable
public data class ListRecordsResponse(
  public val cursor: String? = null,
  public val records: ReadOnlyList<ListRecordsRecord>,
)
