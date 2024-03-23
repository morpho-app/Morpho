package app.bsky.actor

import kotlinx.serialization.Serializable
import com.morpho.butterfly.model.ReadOnlyList
import kotlinx.serialization.SerialName

@Serializable
//@SerialName("app.bsky.actor.GetPreferencesResponse")
public data class GetPreferencesResponse(
  public val preferences: ReadOnlyList<PreferencesUnion>,
)
