package app.bsky.feed

import com.atproto.repo.StrongRef
import kotlinx.serialization.Serializable
import radiant.nimbus.api.model.Timestamp

@Serializable
public data class Repost(
  public val subject: StrongRef,
  public val createdAt: Timestamp,
)
