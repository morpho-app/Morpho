package app.bsky.unspecced

import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri

@Serializable
public data class SkeletonSearchPost(
  public val uri: AtUri,
)
