package app.bsky.unspecced

import kotlin.Any
import kotlin.Boolean
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class SearchActorsSkeletonQueryParams(
  /**
   * search query string; syntax, phrase, boolean, and faceting is unspecified, but Lucene query
   * syntax is recommended. For typeahead search, only simple term match is supported, not full syntax
   */
  public val q: String,
  /**
   * if true, acts as fast/simple 'typeahead' query
   */
  public val typeahead: Boolean? = null,
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
    add("typeahead" to typeahead)
    add("limit" to limit)
    add("cursor" to cursor)
  }.toImmutableList()
}

@Serializable
public data class SearchActorsSkeletonResponse(
  public val cursor: String? = null,
  /**
   * count of search hits. optional, may be rounded/truncated, and may not be possible to paginate
   * through all hits
   */
  public val hitsTotal: Long? = null,
  public val actors: ReadOnlyList<SkeletonSearchActor>,
)
