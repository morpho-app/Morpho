package com.atproto.repo

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid

@Serializable
public data class StrongRef(
  public val uri: AtUri,
  public val cid: Cid,
)
