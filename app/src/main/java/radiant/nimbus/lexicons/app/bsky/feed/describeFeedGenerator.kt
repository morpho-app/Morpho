package app.bsky.feed

import kotlinx.serialization.Serializable
import radiant.nimbus.api.Did
import radiant.nimbus.api.model.ReadOnlyList

@Serializable
public data class DescribeFeedGeneratorResponse(
  public val did: Did,
  public val feeds: ReadOnlyList<DescribeFeedGeneratorFeed>,
  public val links: DescribeFeedGeneratorLinks? = null,
)
