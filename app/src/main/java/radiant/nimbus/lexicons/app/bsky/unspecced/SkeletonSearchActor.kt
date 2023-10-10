package app.bsky.unspecced

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did

@Serializable
public data class SkeletonSearchActor(
  public val did: Did,
)
