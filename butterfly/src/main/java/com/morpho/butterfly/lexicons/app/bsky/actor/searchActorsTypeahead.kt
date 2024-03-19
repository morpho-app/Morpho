package app.bsky.actor

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class SearchActorsTypeaheadQuery(
  /**
   * DEPRECATED: use 'q' instead
   */
  public val term: String? = null,
  /**
   * search query prefix; not a full query string
   */
  public val q: String? = null,
  public val limit: Long? = 10,
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
  }.toImmutableList()
}

@Serializable
public data class SearchActorsTypeaheadResponse(
  public val actors: ReadOnlyList<ProfileViewBasic>,
)
