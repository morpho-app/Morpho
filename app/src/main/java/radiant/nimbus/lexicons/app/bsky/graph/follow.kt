package app.bsky.graph

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.Timestamp

@Serializable
public data class Follow(
  public val subject: Did,
  public val createdAt: Timestamp,
)