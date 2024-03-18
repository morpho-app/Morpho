package com.atproto.admin

import com.atproto.label.Label
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import morpho.app.api.AtUri
import morpho.app.api.Cid
import morpho.app.api.model.ReadOnlyList
import morpho.app.api.model.Timestamp

@Serializable
public data class RecordViewDetail(
  public val uri: AtUri,
  public val cid: Cid,
  public val `value`: JsonElement,
  public val blobs: ReadOnlyList<BlobView>,
  public val labels: ReadOnlyList<Label> = persistentListOf(),
  public val indexedAt: Timestamp,
  public val moderation: ModerationDetail,
  public val repo: RepoView,
)
