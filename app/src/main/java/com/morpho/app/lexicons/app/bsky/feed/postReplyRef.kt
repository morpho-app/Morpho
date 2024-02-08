package app.bsky.feed

import com.atproto.repo.StrongRef
import kotlinx.serialization.Serializable

@Serializable
public data class PostReplyRef(
  public val root: StrongRef,
  public val parent: StrongRef,
)
