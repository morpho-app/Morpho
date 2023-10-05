package app.bsky.graph

import app.bsky.actor.ProfileView
import app.bsky.richtext.Facet
import kotlin.String
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid
import radiant.nimbus.api.model.ReadOnlyList
import radiant.nimbus.api.model.Timestamp

@Serializable
public data class ListView(
  public val uri: AtUri,
  public val cid: Cid,
  public val creator: ProfileView,
  public val name: String,
  public val purpose: Token,
  public val description: String? = null,
  public val descriptionFacets: ReadOnlyList<Facet> = persistentListOf(),
  public val avatar: String? = null,
  public val viewer: ListViewerState? = null,
  public val indexedAt: Timestamp,
) {
  init {
    require(name.count() >= 1) {
      "name.count() must be >= 1, but was ${name.count()}"
    }
    require(name.count() <= 64) {
      "name.count() must be <= 64, but was ${name.count()}"
    }
    require(description == null || description.count() <= 3_000) {
      "description.count() must be <= 3_000, but was ${description?.count()}"
    }
  }
}
