package app.bsky.richtext

import kotlin.String
import kotlinx.serialization.Serializable

/**
 * A hashtag.
 */
@Serializable
public data class FacetTag(
  public val tag: String,
) {
  init {
    require(tag.count() <= 640) {
      "tag.count() must be <= 640, but was ${tag.count()}"
    }
  }
}
