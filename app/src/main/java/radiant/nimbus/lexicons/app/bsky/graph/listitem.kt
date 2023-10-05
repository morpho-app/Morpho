package app.bsky.graph

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.Timestamp

@Serializable
public data class Listitem(
  public val subject: Did,
  public val list: AtUri,
  public val createdAt: Timestamp,
)
