package com.atproto.admin

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class SearchReposQuery(
  /**
   * DEPRECATED: use 'q' instead
   */
  public val term: String? = null,
  public val q: String? = null,
  public val invitedBy: String? = null,
  public val limit: Long? = 50,
  public val cursor: String? = null,
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
    add("term" to term)
    add("q" to q)
    add("invitedBy" to invitedBy)
    add("limit" to limit)
    add("cursor" to cursor)
  }.toImmutableList()
}

@Serializable
public data class SearchReposResponse(
  public val cursor: String? = null,
  public val repos: ReadOnlyList<RepoView>,
)
