package app.bsky.richtext

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did

/**
 * A facet feature for actor mentions.
 */
@Serializable
public data class FacetMention(
  public val did: Did,
)
