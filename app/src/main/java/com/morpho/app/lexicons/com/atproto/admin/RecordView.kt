package com.atproto.admin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import morpho.app.api.AtUri
import morpho.app.api.Cid
import morpho.app.api.model.ReadOnlyList
import morpho.app.api.model.Timestamp

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
