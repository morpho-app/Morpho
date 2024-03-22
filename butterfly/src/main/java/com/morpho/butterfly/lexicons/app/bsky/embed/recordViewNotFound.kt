package app.bsky.embed

import kotlin.Boolean
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri

@Serializable
public data class RecordViewNotFound(
  public val uri: AtUri,
  public val notFound: Boolean,
)
