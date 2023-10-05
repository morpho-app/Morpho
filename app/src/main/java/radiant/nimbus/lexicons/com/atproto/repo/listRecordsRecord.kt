package com.atproto.repo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid

@Serializable
public data class ListRecordsRecord(
  public val uri: AtUri,
  public val cid: Cid,
  public val `value`: JsonElement,
)
