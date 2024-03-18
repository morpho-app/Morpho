package com.atproto.admin

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetModerationActionsQueryParams(
  public val subject: String? = null,
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
    add("subject" to subject)
    add("limit" to limit)
    add("cursor" to cursor)
  }.toImmutableList()
}

@Serializable
public data class GetModerationActionsResponse(
  public val cursor: String? = null,
  public val actions: ReadOnlyList<ActionView>,
)
