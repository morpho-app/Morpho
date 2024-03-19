package app.bsky.feed

import app.bsky.actor.ProfileView
import app.bsky.richtext.Facet
import kotlin.Long
import kotlin.String
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class GeneratorView(
  public val uri: AtUri,
  public val cid: Cid,
  public val did: Did,
  public val creator: ProfileView,
  public val displayName: String,
  public val description: String? = null,
  public val descriptionFacets: ReadOnlyList<Facet> = persistentListOf(),
  public val avatar: String? = null,
  public val likeCount: Long? = null,
  public val viewer: GeneratorViewerState? = null,
  public val indexedAt: Timestamp,
) {
  init {
    require(description == null || description.count() <= 3_000) {
      "description.count() must be <= 3_000, but was ${description?.count()}"
    }
    require(likeCount == null || likeCount >= 0) {
      "likeCount must be >= 0, but was $likeCount"
    }
  }
}
