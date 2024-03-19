package app.bsky.feed

import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetAuthorFeedQuery(
  public val actor: AtIdentifier,
  public val limit: Long? = 50,
  public val cursor: String? = null,
  public val filter: GetAuthorFeedFilter? = GetAuthorFeedFilter.POSTS_WITH_REPLIES,
) {
  init {
    require(limit == null || limit >= 1) {
      "limit must be >= 1, but was $limit"
    }
    require(limit == null || limit <= 100) {
      "limit must be <= 100, but was $limit"
    }
  }

  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("actor" to actor)
    add("limit" to limit)
    add("cursor" to cursor)
    add("filter" to filter?.n)
  }.toImmutableList()
}

@Serializable
public data class GetAuthorFeedResponse(
  public val cursor: String? = null,
  public val feed: ReadOnlyList<FeedViewPost>,
)
