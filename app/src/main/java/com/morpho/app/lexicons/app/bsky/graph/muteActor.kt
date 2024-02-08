package app.bsky.graph

import kotlinx.serialization.Serializable
import morpho.app.api.AtIdentifier

@Serializable
public data class MuteActorRequest(
  public val actor: AtIdentifier,
)
