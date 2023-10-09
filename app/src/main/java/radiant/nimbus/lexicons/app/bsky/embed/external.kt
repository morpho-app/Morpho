package app.bsky.embed

import kotlinx.serialization.Serializable

@Serializable
public data class External(
  public val `external`: ExternalExternal,
)

@Serializable
public data class ExternalMain(
  public val `external`: ExternalExternal,
)