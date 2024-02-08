package app.bsky.richtext

import kotlin.Long
import kotlinx.serialization.Serializable

/**
 * A text segment. Start is inclusive, end is exclusive. Indices are for utf8-encoded strings.
 */
@Serializable
public data class FacetByteSlice(
  public val byteStart: Long,
  public val byteEnd: Long,
) {
  init {
    require(byteStart >= 0) {
      "byteStart must be >= 0, but was $byteStart"
    }
    require(byteEnd >= 0) {
      "byteEnd must be >= 0, but was $byteEnd"
    }
  }
}
