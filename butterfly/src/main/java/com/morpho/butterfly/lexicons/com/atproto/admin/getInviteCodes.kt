package com.atproto.admin

import com.atproto.server.InviteCode
import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetInviteCodesQuery(
  public val sort: GetInviteCodesSort? = GetInviteCodesSort.RECENT,
  public val limit: Long? = 100,
  public val cursor: String? = null,
) {
  init {
    require(limit == null || limit >= 1) {
      "limit must be >= 1, but was $limit"
    }
    require(limit == null || limit <= 500) {
      "limit must be <= 500, but was $limit"
    }
  }

  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("sort" to sort)
    add("limit" to limit)
    add("cursor" to cursor)
  }.toImmutableList()
}

@Serializable
public data class GetInviteCodesResponse(
  public val cursor: String? = null,
  public val codes: ReadOnlyList<InviteCode>,
)
