package app.bsky.graph

import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.model.Timestamp

@Serializable
public data class Listblock(
  public val subject: AtUri,
  public val createdAt: Timestamp,
)
