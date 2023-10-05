package com.atproto.sync

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Cid
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class GetHeadQueryParams(
  /**
   * The DID of the repo.
   */
  public val did: Did,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("did" to did)
  }.toImmutableList()
}

@Serializable
public data class GetHeadResponse(
  public val root: Cid,
)
