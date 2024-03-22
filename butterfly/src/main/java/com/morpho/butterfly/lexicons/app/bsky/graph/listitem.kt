package app.bsky.graph

import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class Listitem(
  public val subject: Did,
  public val list: AtUri,
  public val createdAt: Timestamp,
)
