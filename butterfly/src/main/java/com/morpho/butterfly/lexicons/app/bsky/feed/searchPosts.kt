package app.bsky.feed

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class SearchPostsQuery(
  /**
   * search query string; syntax, phrase, boolean, and faceting is unspecified, but Lucene query
   * syntax is recommended
   */
  public val q: String,
  public val limit: Long? = 25,
  /**
   * optional pagination mechanism; may not necessarily allow scrolling through entire result set
   */
  public val cursor: String? = null,
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
    add("q" to q)
    add("limit" to limit)
    add("cursor" to cursor)
  }.toImmutableList()
}

@Serializable
public data class SearchPostsResponse(
  public val cursor: String? = null,
  /**
   * count of search hits. optional, may be rounded/truncated, and may not be possible to paginate
   * through all hits
   */
  public val hitsTotal: Long? = null,
  public val posts: ReadOnlyList<PostView>,
)
