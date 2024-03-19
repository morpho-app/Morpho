package app.bsky.richtext

import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did

/**
 * A facet feature for actor mentions.
 */
@Serializable
public data class FacetMention(
  public val did: Did,
)
