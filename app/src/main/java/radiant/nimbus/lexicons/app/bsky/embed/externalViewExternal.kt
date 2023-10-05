package app.bsky.embed

import kotlin.String
import kotlinx.serialization.Serializable
import radiant.nimbus.api.Uri

@Serializable
public data class ExternalViewExternal(
  public val uri: Uri,
  public val title: String,
  public val description: String,
  public val thumb: String? = null,
)
