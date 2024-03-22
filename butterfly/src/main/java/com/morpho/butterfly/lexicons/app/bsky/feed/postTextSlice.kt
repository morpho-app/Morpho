package app.bsky.feed

import kotlin.Long
import kotlinx.serialization.Serializable

/**
 * Deprecated. Use app.bsky.richtext instead -- A text segment. Start is inclusive, end is
 * exclusive. Indices are for utf16-encoded strings.
 */
@Serializable
public data class PostTextSlice(
  public val start: Long,
  public val end: Long,
) {
  init {
    require(start >= 0) {
      "start must be >= 0, but was $start"
    }
    require(end >= 0) {
      "end must be >= 0, but was $end"
    }
  }
}
