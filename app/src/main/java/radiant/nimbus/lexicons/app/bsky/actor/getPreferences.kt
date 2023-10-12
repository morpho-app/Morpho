package app.bsky.actor

import kotlinx.serialization.Serializable
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class GetPreferencesResponse(
  public val preferences: ReadOnlyList<PreferencesUnion>,
)
