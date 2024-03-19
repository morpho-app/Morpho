package app.bsky.actor

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class SearchActorsQuery(
  /**
   * DEPRECATED: use 'q' instead
   */
  public val term: String? = null,
  /**
   * search query string; syntax, phrase, boolean, and faceting is unspecified, but Lucene query
   * syntax is recommended
   */
  public val q: String? = null,
  public val limit: Long? = 25,
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
    add("term" to term)
    add("q" to q)
    add("limit" to limit)
    add("cursor" to cursor)
  }.toImmutableList()
}

@Serializable
public data class SearchActorsResponse(
  public val cursor: String? = null,
  public val actors: ReadOnlyList<ProfileView>,
)
