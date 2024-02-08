package app.bsky.graph

import kotlinx.serialization.Serializable
import morpho.app.api.Did
import morpho.app.api.model.Timestamp

@Serializable
public data class Follow(
  public val subject: Did,
  public val createdAt: Timestamp,
)
