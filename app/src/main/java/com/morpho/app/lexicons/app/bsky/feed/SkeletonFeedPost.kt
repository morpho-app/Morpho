package app.bsky.feed

import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import morpho.app.api.AtUri
import morpho.app.api.runtime.valueClassSerializer

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