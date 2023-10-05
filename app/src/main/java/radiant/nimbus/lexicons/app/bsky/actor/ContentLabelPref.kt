package app.bsky.actor

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class ContentLabelPref(
  public val label: String,
  public val visibility: Visibility,
)
