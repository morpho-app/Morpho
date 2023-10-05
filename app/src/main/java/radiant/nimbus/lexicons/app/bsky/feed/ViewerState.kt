package app.bsky.feed

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri

@Serializable
public data class ViewerState(
  public val repost: AtUri? = null,
  public val like: AtUri? = null,
)
