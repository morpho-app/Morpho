package app.bsky.embed

import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.Uri

@Serializable
public data class ExternalExternal(
  public val uri: Uri,
  public val title: String,
  public val description: String,
  public val thumb: JsonElement? = null,
)
