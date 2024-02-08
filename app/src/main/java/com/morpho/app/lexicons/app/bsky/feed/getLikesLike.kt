package app.bsky.feed

import app.bsky.actor.ProfileView
import kotlinx.serialization.Serializable
import morpho.app.api.model.Timestamp

@Serializable
public data class GetLikesLike(
  public val indexedAt: Timestamp,
  public val createdAt: Timestamp,
  public val actor: ProfileView,
)
