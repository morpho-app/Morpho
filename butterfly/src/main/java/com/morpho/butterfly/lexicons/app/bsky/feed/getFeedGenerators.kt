package app.bsky.feed

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetFeedGeneratorsQuery(
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
