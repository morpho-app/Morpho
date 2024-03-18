package app.bsky.feed

import com.atproto.repo.StrongRef
import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class Repost(
  public val subject: StrongRef,
  public val createdAt: Timestamp,
)
