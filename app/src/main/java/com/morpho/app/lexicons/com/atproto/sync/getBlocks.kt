package com.atproto.sync

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.Cid
import morpho.app.api.Did
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class GetBlocksQueryParams(
  /**
   * The DID of the repo.
   */
  public val did: Did,
  public val cids: ReadOnlyList<Cid>,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("did" to did)
    cids.forEach {
      add("cids" to it)
    }
  }.toImmutableList()
}
