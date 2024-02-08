package app.bsky.feed

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class GetFeedGeneratorsQueryParams(
  public val feeds: ReadOnlyList<AtUri>,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    feeds.forEach {
      add("feeds" to it)
    }
  }.toImmutableList()
}

@Serializable
public data class GetFeedGeneratorsResponse(
  public val feeds: ReadOnlyList<GeneratorView>,
)
