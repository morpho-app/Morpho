package app.bsky.actor

import app.bsky.graph.ListViewBasic
import kotlin.Boolean
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri

@Serializable
public data class ViewerState(
  public val muted: Boolean? = null,
  public val mutedByList: ListViewBasic? = null,
  public val blockedBy: Boolean? = null,
  public val blocking: AtUri? = null,
  public val following: AtUri? = null,
  public val followedBy: AtUri? = null,
)
