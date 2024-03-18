package app.bsky.feed

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class DescribeFeedGeneratorLinks(
  public val privacyPolicy: String? = null,
  public val termsOfService: String? = null,
)
