package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class CreateAppPasswordRequest(
  public val name: String,
)

public typealias CreateAppPasswordResponse = AppPasswordInfo
