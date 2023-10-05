package app.bsky.actor

import kotlinx.serialization.Serializable

@Serializable
public data class GetPreferencesResponse(
  public val preferences: PreferencesUnion,
)
