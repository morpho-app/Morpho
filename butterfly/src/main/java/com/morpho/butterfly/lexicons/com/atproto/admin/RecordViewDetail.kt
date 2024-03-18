package com.atproto.admin

import com.atproto.label.Label
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp

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
