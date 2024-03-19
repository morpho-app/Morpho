package app.bsky.feed

import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.valueClassSerializer

@Serializable
public sealed interface SkeletonFeedPostReasonUnion {
  public class SkeletonReasonRepostSerializer : KSerializer<SkeletonReasonRepost> by
      valueClassSerializer()

  @Serializable(with = SkeletonReasonRepostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#skeletonReasonRepost")
  public value class SkeletonReasonRepost(
    public val `value`: app.bsky.feed.SkeletonReasonRepost,
  ) : SkeletonFeedPostReasonUnion
}

@Serializable
public data class SkeletonFeedPost(
  public val post: AtUri,
  public val reason: SkeletonFeedPostReasonUnion? = null,
)
