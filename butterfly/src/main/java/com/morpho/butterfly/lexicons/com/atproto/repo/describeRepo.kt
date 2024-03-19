package com.atproto.repo

import kotlin.Any
import kotlin.Boolean
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.Nsid
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class DescribeRepoQuery(
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
