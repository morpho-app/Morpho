package app.bsky.embed

import app.bsky.feed.BlockedAuthor
import kotlin.Boolean
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri

@Serializable
public data class RecordViewBlocked(
  public val uri: AtUri,
  public val blocked: Boolean,
  public val author: BlockedAuthor,
)
