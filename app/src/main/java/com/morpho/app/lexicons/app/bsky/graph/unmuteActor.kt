package app.bsky.graph

import kotlinx.serialization.Serializable
import morpho.app.api.AtIdentifier

@Serializable
public data class UnmuteActorRequest(
  public val actor: AtIdentifier,
)
