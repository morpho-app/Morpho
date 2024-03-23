package app.bsky.actor

import com.atproto.label.Label
import kotlin.String
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class ProfileView(
  public val did: Did,
  public val handle: Handle,
  public val displayName: String? = null,
  public val description: String? = null,
  public val avatar: String? = null,
  public val indexedAt: Timestamp? = null,
  public val associated: ProfileAssociated? = null,
  public val viewer: ViewerState? = null,
  public val labels: ReadOnlyList<Label> = persistentListOf(),
) {
  init {
    require(displayName == null || displayName.count() <= 640) {
      "displayName.count() must be <= 640, but was ${displayName?.count()}"
    }
    require(description == null || description.count() <= 2_560) {
      "description.count() must be <= 2_560, but was ${description?.count()}"
    }
  }
}
