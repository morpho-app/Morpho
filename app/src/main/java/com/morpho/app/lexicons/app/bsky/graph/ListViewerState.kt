package app.bsky.graph

import kotlin.Boolean
import kotlinx.serialization.Serializable
import morpho.app.api.AtUri

@Serializable
public data class ListViewerState(
  public val muted: Boolean? = null,
  public val blocked: AtUri? = null,
)
