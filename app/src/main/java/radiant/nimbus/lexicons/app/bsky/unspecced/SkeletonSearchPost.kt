package app.bsky.unspecced

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri

@Serializable
public data class SkeletonSearchPost(
  public val uri: AtUri,
)
