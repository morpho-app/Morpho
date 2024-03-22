package app.bsky.notification

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class ListNotificationsQuery(
  public val limit: Long? = 50,
  public val cursor: String? = null,
  public val seenAt: Timestamp? = null,
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
    add("limit" to limit)
    add("cursor" to cursor)
    add("seenAt" to seenAt)
  }.toImmutableList()
}

@Serializable
public data class ListNotificationsResponse(
  public val cursor: String? = null,
  public val notifications: ReadOnlyList<ListNotificationsNotification>,
)
