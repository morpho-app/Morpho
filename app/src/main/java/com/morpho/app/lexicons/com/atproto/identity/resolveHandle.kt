package com.atproto.identity

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.Handle
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class ResolveHandleQueryParams(
  /**
   * The handle to resolve.
   */
  public val handle: Handle,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("handle" to handle)
  }.toImmutableList()
}

@Serializable
public data class ResolveHandleResponse(
  public val did: Did,
)
