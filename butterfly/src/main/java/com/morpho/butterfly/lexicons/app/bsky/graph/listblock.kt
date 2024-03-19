package app.bsky.graph

import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class Listblock(
  public val subject: AtUri,
  public val createdAt: Timestamp,
)
