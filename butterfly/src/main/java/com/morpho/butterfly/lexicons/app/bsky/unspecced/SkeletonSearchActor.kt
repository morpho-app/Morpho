package app.bsky.unspecced

import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did

@Serializable
public data class SkeletonSearchActor(
  public val did: Did,
)
