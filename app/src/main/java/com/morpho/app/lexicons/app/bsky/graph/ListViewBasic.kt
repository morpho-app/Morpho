package app.bsky.graph

import kotlin.String
import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.Cid
import morpho.app.api.model.Timestamp

@Serializable
public data class ListViewBasic(
  public val uri: AtUri,
  public val cid: Cid,
  public val name: String,
  public val purpose: Token,
  public val avatar: String? = null,
  public val viewer: ListViewerState? = null,
  public val indexedAt: Timestamp? = null,
) {
  init {
    require(name.count() >= 1) {
      "name.count() must be >= 1, but was ${name.count()}"
    }
    require(name.count() <= 64) {
      "name.count() must be <= 64, but was ${name.count()}"
    }
  }
}