package com.atproto.admin

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri

@Serializable
public data class RecordViewNotFound(
  public val uri: AtUri,
)
