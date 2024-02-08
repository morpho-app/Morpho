package com.atproto.repo

import kotlin.Any
import kotlin.Boolean
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import morpho.app.api.AtIdentifier
import morpho.app.api.Did
import morpho.app.api.Handle
import morpho.app.api.Nsid
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class DescribeRepoQueryParams(
  /**
   * The handle or DID of the repo.
   */
  public val repo: AtIdentifier,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("repo" to repo)
  }.toImmutableList()
}

@Serializable
public data class DescribeRepoResponse(
  public val handle: Handle,
  public val did: Did,
  public val didDoc: JsonElement,
  public val collections: ReadOnlyList<Nsid>,
  public val handleIsCorrect: Boolean,
)
