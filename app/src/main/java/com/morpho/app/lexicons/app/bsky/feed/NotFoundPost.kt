package app.bsky.feed

import kotlin.Boolean
import kotlinx.serialization.Serializable
import morpho.app.api.AtUri

@Serializable
public data class NotFoundPost(
  public val uri: AtUri,
  public val notFound: Boolean,
)
