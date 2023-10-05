package com.atproto.admin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid
import radiant.nimbus.api.model.ReadOnlyList
import radiant.nimbus.api.model.Timestamp

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
