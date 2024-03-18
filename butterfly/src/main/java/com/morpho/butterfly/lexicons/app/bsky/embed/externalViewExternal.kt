package app.bsky.embed

import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Uri

@Serializable
public data class ExternalViewExternal(
  public val uri: Uri,
  public val title: String,
  public val description: String,
  public val thumb: String? = null,
)
