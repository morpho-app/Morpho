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
public data class GetUnreadCountQuery(
  public val seenAt: Timestamp? = null,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("seenAt" to seenAt)
  }.toImmutableList()
}

@Serializable
public data class GetUnreadCountResponse(
  public val count: Long,
)
