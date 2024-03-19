package app.bsky.actor

import kotlin.Boolean
import kotlinx.serialization.Serializable

@Serializable
public data class AdultContentPref(
  public val enabled: Boolean,
)
