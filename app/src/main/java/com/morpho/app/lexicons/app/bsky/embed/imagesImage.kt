package app.bsky.embed

import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class ImagesImage(
  public val image: JsonElement,
  public val alt: String,
  public val aspectRatio: ImagesAspectRatio? = null,
)
