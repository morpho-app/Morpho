package com.atproto.admin

import kotlinx.serialization.Serializable
import morpho.app.api.AtUri

@Serializable
public data class RecordViewNotFound(
  public val uri: AtUri,
)
