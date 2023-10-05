package app.bsky.graph

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri

@Serializable
public data class UnmuteActorListRequest(
  public val list: AtUri,
)
