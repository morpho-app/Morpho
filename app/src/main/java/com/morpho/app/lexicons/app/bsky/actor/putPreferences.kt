package app.bsky.actor

import kotlinx.serialization.Serializable

@Serializable
public data class PutPreferencesRequest(
  public val preferences: PreferencesUnion,
)
