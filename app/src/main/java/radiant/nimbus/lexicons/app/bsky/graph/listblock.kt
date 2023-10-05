package app.bsky.graph

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.Timestamp

@Serializable
public data class Listblock(
  public val subject: AtUri,
  public val createdAt: Timestamp,
)
