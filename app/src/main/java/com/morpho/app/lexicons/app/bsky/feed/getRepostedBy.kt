package app.bsky.feed

import app.bsky.actor.ProfileView
import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.Cid
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class GetRepostedByQueryParams(
  public val uri: AtUri,
  public val cid: Cid? = null,
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
    add("uri" to uri)
    add("cid" to cid)
    add("limit" to limit)
    add("cursor" to cursor)
  }.toImmutableList()
}

@Serializable
public data class GetRepostedByResponse(
  public val uri: AtUri,
  public val cid: Cid? = null,
  public val cursor: String? = null,
  public val repostedBy: ReadOnlyList<ProfileView>,
)
