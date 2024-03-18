package app.bsky.graph

import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri

@Serializable
public data class MuteActorListRequest(
  public val list: AtUri,
)
