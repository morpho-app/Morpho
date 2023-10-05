package app.bsky.feed

import app.bsky.graph.ListViewBasic
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class ThreadgateView(
  public val uri: AtUri? = null,
  public val cid: Cid? = null,
  public val record: JsonElement? = null,
  public val lists: ReadOnlyList<ListViewBasic> = persistentListOf(),
)
