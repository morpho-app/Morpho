package app.bsky.feed

import kotlin.String
import kotlinx.serialization.Serializable

/**
 * Deprecated: use facets instead.
 */
@Serializable
public data class PostEntity(
  public val index: PostTextSlice,
  /**
   * Expected values are 'mention' and 'link'.
   */
  public val type: String,
  public val `value`: String,
)
