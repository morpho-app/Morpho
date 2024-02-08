package app.bsky.graph

import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.Did
import morpho.app.api.model.Timestamp

@Serializable
public data class Listitem(
  public val subject: Did,
  public val list: AtUri,
  public val createdAt: Timestamp,
)
