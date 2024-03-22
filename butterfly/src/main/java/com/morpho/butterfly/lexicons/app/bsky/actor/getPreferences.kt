package app.bsky.actor

import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class GetPreferencesResponse(
  public val preferences: ReadOnlyList<PreferencesUnion>,
)
