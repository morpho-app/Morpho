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
public data class GetLatestCommitQueryParams(
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
public data class GetLatestCommitResponse(
  public val cid: Cid,
  public val rev: String,
)
