package app.bsky.feed

import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri

@Serializable
public data class GeneratorViewerState(
  public val like: AtUri? = null,
)
