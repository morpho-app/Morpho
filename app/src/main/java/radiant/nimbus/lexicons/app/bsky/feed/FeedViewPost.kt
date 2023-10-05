package app.bsky.feed

import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import radiant.nimbus.api.runtime.valueClassSerializer

@Serializable
public sealed interface FeedViewPostReasonUnion {
  public class ReasonRepostSerializer : KSerializer<ReasonRepost> by valueClassSerializer()

  @Serializable(with = ReasonRepostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#reasonRepost")
  public value class ReasonRepost(
    public val `value`: app.bsky.feed.ReasonRepost,
  ) : FeedViewPostReasonUnion
}

@Serializable
public data class FeedViewPost(
  public val post: PostView,
  public val reply: ReplyRef? = null,
  public val reason: FeedViewPostReasonUnion? = null,
)
