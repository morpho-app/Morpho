package com.atproto.repo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid

@Serializable
public data class ListRecordsRecord(
  public val uri: AtUri,
  public val cid: Cid,
  public val `value`: JsonElement,
)
