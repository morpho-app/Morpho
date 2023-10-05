package app.bsky.feed

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri

@Serializable
public data class GeneratorViewerState(
  public val like: AtUri? = null,
)
