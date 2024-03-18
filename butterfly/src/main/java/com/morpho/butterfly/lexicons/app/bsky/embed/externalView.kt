package app.bsky.embed

import kotlinx.serialization.Serializable

@Serializable
public data class ExternalView(
  public val `external`: ExternalViewExternal,
)
