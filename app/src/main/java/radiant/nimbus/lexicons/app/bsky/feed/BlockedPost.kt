package app.bsky.feed

import kotlin.Boolean
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri

@Serializable
public data class BlockedPost(
  public val uri: AtUri,
  public val blocked: Boolean,
  public val author: BlockedAuthor,
)
