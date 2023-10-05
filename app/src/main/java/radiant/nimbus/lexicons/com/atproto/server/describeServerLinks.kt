package com.atproto.server

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class DescribeServerLinks(
  public val privacyPolicy: String? = null,
  public val termsOfService: String? = null,
)
