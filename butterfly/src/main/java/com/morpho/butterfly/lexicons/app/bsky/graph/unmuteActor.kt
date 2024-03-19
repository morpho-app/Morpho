package app.bsky.graph

import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtIdentifier

@Serializable
public data class UnmuteActorRequest(
  public val actor: AtIdentifier,
)
