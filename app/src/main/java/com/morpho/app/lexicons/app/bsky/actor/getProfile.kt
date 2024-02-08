package app.bsky.actor

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.AtIdentifier
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class GetProfileQueryParams(
  public val actor: AtIdentifier,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("actor" to actor)
  }.toImmutableList()
}

public typealias GetProfileResponse = ProfileViewDetailed
