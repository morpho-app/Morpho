package com.atproto.repo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import morpho.app.api.AtUri
import morpho.app.api.Cid

@Serializable
public data class ListRecordsRecord(
  public val uri: AtUri,
  public val cid: Cid,
  public val `value`: JsonElement,
)
