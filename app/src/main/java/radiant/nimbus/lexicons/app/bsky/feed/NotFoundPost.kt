package app.bsky.feed

import kotlin.Boolean
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri

@Serializable
public data class NotFoundPost(
  public val uri: AtUri,
  public val notFound: Boolean,
)
