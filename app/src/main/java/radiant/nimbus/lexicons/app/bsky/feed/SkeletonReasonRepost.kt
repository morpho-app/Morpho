package app.bsky.feed

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri

@Serializable
public data class SkeletonReasonRepost(
  public val repost: AtUri,
)
