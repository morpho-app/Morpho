package app.bsky.graph

import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class Block(
  public val subject: Did,
  public val createdAt: Timestamp,
)
