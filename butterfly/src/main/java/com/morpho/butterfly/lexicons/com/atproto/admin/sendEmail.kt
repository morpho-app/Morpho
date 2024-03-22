package com.atproto.admin

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did

@Serializable
public data class SendEmailRequest(
  public val recipientDid: Did,
  public val content: String,
  public val subject: String? = null,
)

@Serializable
public data class SendEmailResponse(
  public val sent: Boolean,
)
