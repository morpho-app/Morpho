package app.bsky.graph

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtIdentifier

@Serializable
public data class MuteActorRequest(
  public val actor: AtIdentifier,
)
