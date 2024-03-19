package com.atproto.admin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.model.ReadOnlyList
import com.morpho.butterfly.model.Timestamp

@Serializable
public data class RecordView(
  public val uri: AtUri,
  public val cid: Cid,
  public val `value`: JsonElement,
  public val blobCids: ReadOnlyList<Cid>,
  public val indexedAt: Timestamp,
  public val moderation: Moderation,
  public val repo: RepoView,
)
