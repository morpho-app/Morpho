package com.atproto.sync

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class GetRepoQueryParams(
  /**
   * The DID of the repo.
   */
  public val did: Did,
  /**
   * The revision of the repo to catch up from.
   */
  public val since: String? = null,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("did" to did)
    add("since" to since)
  }.toImmutableList()
}