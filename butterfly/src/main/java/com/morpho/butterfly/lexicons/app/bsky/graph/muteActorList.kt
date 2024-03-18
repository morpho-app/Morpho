package app.bsky.graph

import kotlinx.serialization.Serializable
import morpho.app.api.AtUri

@Serializable
public data class MuteActorListRequest(
  public val list: AtUri,
)
