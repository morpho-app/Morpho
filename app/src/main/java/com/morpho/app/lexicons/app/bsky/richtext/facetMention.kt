package app.bsky.richtext

import kotlinx.serialization.Serializable
import morpho.app.api.Did

/**
 * A facet feature for actor mentions.
 */
@Serializable
public data class FacetMention(
  public val did: Did,
)
