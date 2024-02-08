package app.bsky.richtext

import kotlinx.serialization.Serializable
import morpho.app.api.Uri

/**
 * A facet feature for links.
 */
@Serializable
public data class FacetLink(
  public val uri: Uri,
)
