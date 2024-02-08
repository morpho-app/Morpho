package app.bsky.feed

import kotlinx.serialization.Serializable
import morpho.app.api.AtUri

@Serializable
public data class SkeletonReasonRepost(
  public val repost: AtUri,
)
