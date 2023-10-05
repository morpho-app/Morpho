package app.bsky.feed

import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri

@Serializable
public data class DescribeFeedGeneratorFeed(
  public val uri: AtUri,
)
