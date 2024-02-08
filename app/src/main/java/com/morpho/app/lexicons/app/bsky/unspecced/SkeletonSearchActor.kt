package app.bsky.unspecced

import kotlinx.serialization.Serializable
import morpho.app.api.Did

@Serializable
public data class SkeletonSearchActor(
  public val did: Did,
)
