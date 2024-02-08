package app.bsky.feed

import app.bsky.actor.ProfileViewBasic
import kotlinx.serialization.Serializable
import morpho.app.api.model.Timestamp

@Serializable
public data class ReasonRepost(
  public val `by`: ProfileViewBasic,
  public val indexedAt: Timestamp,
)
