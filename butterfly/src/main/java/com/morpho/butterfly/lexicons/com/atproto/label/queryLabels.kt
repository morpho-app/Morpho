package com.atproto.label

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class QueryLabels(
  /**
   * List of AT URI patterns to match (boolean 'OR'). Each may be a prefix (ending with '*'; will
   * match inclusive of the string leading to '*'), or a full URI
   */
  public val uriPatterns: ReadOnlyList<String>,
  /**
   * Optional list of label sources (DIDs) to filter on
   */
  public val sources: ReadOnlyList<Did> = persistentListOf(),
  public val limit: Long? = 50,
  public val cursor: String? = null,
) {
  init {
    require(limit == null || limit >= 1) {
      "limit must be >= 1, but was $limit"
    }
    require(limit == null || limit <= 250) {
      "limit must be <= 250, but was $limit"
    }
  }

  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    uriPatterns.forEach {
      add("uriPatterns" to it)
    }
    sources.forEach {
      add("sources" to it)
    }
    add("limit" to limit)
    add("cursor" to cursor)
  }.toImmutableList()
}

@Serializable
public data class QueryLabelsResponse(
  public val cursor: String? = null,
  public val labels: ReadOnlyList<Label>,
)
