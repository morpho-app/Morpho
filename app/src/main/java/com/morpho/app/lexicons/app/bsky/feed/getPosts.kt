package app.bsky.feed

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class GetPostsQueryParams(
  public val uris: ReadOnlyList<AtUri>,
) {
  init {
    require(uris.count() <= 25) {
      "uris.count() must be <= 25, but was ${uris.count()}"
    }
  }

  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    uris.forEach {
      add("uris" to it)
    }
  }.toImmutableList()
}

@Serializable
public data class GetPostsResponse(
  public val posts: ReadOnlyList<PostView>,
)
