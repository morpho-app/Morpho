package app.bsky.graph

import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtIdentifier

@Serializable
public data class MuteActorRequest(
  public val actor: AtIdentifier,
)
