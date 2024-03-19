package app.bsky.feed

import com.morpho.butterfly.AtUri
import kotlin.Boolean
import kotlinx.serialization.Serializable


@Serializable
public data class NotFoundPost(
  public val uri: AtUri,
  public val notFound: Boolean,
)
