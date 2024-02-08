package app.bsky.embed

import kotlin.Long
import kotlinx.serialization.Serializable

/**
 * width:height represents an aspect ratio. It may be approximate, and may not correspond to
 * absolute dimensions in any given unit.
 */
@Serializable
public data class ImagesAspectRatio(
  public val width: Long,
  public val height: Long,
) {
  init {
    require(width >= 1) {
      "width must be >= 1, but was $width"
    }
    require(height >= 1) {
      "height must be >= 1, but was $height"
    }
  }
}
