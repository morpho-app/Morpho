package app.bsky.feed

import kotlin.Boolean
import kotlinx.serialization.Serializable

@Serializable
public data class ViewerThreadState(
  public val canReply: Boolean? = null,
)
