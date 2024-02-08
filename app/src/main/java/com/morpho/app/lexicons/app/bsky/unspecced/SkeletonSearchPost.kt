package app.bsky.unspecced

import kotlinx.serialization.Serializable
import morpho.app.api.AtUri

@Serializable
public data class SkeletonSearchPost(
  public val uri: AtUri,
)
