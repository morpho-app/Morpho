package app.bsky.graph

import app.bsky.actor.ProfileView
import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class GetFollowsQueryParams(
  public val actor: AtIdentifier,
  public val limit: Long? = 50,
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
    add("actor" to actor)
    add("limit" to limit)
    add("cursor" to cursor)
  }.toImmutableList()
}

@Serializable
public data class GetFollowsResponse(
  public val subject: ProfileView,
  public val cursor: String? = null,
  public val follows: ReadOnlyList<ProfileView>,
)
