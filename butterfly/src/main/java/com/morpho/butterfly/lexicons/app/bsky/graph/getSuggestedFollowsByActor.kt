package app.bsky.graph

import app.bsky.actor.ProfileView
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetSuggestedFollowsByActorQuery(
  public val actor: AtIdentifier,
) {
  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("actor" to actor)
  }.toImmutableList()
}

@Serializable
public data class GetSuggestedFollowsByActorResponse(
  public val suggestions: ReadOnlyList<ProfileView>,
)
