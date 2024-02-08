package app.bsky.actor

import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import morpho.app.api.AtIdentifier
import morpho.app.api.model.ReadOnlyList

@Serializable
public data class GetProfilesQueryParams(
  public val actors: ReadOnlyList<AtIdentifier>,
) {
  init {
    require(actors.count() <= 25) {
      "actors.count() must be <= 25, but was ${actors.count()}"
    }
  }

  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    actors.forEach {
      add("actors" to it)
    }
  }.toImmutableList()
}

@Serializable
public data class GetProfilesResponse(
  public val profiles: ReadOnlyList<ProfileViewDetailed>,
)
