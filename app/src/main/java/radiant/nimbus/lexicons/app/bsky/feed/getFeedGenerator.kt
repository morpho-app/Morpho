package app.bsky.feed

import kotlin.Any
import kotlin.Boolean
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class GetFeedGeneratorQueryParams(
  public val feed: AtUri,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("feed" to feed)
  }.toImmutableList()
}

@Serializable
public data class GetFeedGeneratorResponse(
  public val view: GeneratorView,
  public val isOnline: Boolean,
  public val isValid: Boolean,
)
