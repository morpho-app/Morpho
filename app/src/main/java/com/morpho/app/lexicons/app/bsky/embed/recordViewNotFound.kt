package app.bsky.embed

import kotlin.Boolean
import kotlinx.serialization.Serializable
import morpho.app.api.AtUri

@Serializable
public data class RecordViewNotFound(
  public val uri: AtUri,
  public val notFound: Boolean,
)
