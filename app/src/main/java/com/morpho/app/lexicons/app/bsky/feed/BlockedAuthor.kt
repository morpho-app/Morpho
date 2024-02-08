package app.bsky.feed

import app.bsky.actor.ViewerState
import kotlinx.serialization.Serializable
import morpho.app.api.Did

@Serializable
public data class BlockedAuthor(
  public val did: Did,
  public val viewer: ViewerState? = null,
)
