package app.bsky.feed

import kotlinx.serialization.Serializable
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.ReadOnlyList

@Serializable
public data class DescribeFeedGeneratorResponse(
  public val did: Did,
  public val feeds: ReadOnlyList<DescribeFeedGeneratorFeed>,
  public val links: DescribeFeedGeneratorLinks? = null,
)
