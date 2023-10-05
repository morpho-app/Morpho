package com.atproto.repo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class UploadBlobResponse(
  public val blob: JsonElement,
)
