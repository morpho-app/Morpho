package app.bsky.embed

import kotlinx.serialization.Serializable

@Serializable
public data class External(
  public val `external`: ExternalExternal,
)
