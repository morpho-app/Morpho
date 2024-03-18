package com.atproto.admin

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetModerationActionQueryParams(
  public val id: Long,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("id" to id)
  }.toImmutableList()
}

public typealias GetModerationActionResponse = ActionViewDetail
