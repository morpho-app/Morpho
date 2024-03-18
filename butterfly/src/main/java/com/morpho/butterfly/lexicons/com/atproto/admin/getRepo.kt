package com.atproto.admin

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetRepoQueryParams(
  public val did: Did,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("did" to did)
  }.toImmutableList()
}

public typealias GetRepoResponse = RepoViewDetail
