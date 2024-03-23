package app.bsky.actor

import com.atproto.label.Label
import kotlin.Long
import kotlin.String
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp
import kotlinx.serialization.SerialName

@Serializable
public data class ProfileViewDetailed(
  public val did: Did,
  public val handle: Handle,
  public val displayName: String? = null,
  public val description: String? = null,
  public val avatar: String? = null,
  public val banner: String? = null,
  public val followersCount: Long? = null,
  public val followsCount: Long? = null,
  public val postsCount: Long? = null,
  public val associated: ProfileAssociated? = null,
  public val indexedAt: Timestamp? = null,
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


@Serializable
@SerialName("app.bsky.actor.defs#profileAssociated")
public data class ProfileAssociated(
  val lists: Long? = null,
  val feedGens: Long? = null,
  val labeler: Boolean? = null,
)