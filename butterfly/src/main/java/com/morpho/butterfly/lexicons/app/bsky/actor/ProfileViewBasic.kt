package app.bsky.actor

import com.atproto.label.Label
import kotlin.String
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class ProfileViewBasic(
  public val did: Did,
  public val handle: Handle,
  public val displayName: String? = null,
  public val avatar: String? = null,
  public val viewer: ViewerState? = null,
  public val associated: ProfileAssociated? = null,
  public val labels: ReadOnlyList<Label> = persistentListOf(),
) {
  init {
    require(displayName == null || displayName.count() <= 640) {
      "displayName.count() must be <= 640, but was ${displayName?.count()}"
    }
  }
}
