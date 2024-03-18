package app.bsky.feed

import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.morpho.butterfly.valueClassSerializer

@Serializable
public sealed interface ReplyRefRootUnion {
  public class PostViewSerializer : KSerializer<PostView> by valueClassSerializer()

  @Serializable(with = PostViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#postView")
  public value class PostView(
    public val `value`: app.bsky.feed.PostView,
  ) : ReplyRefRootUnion

  public class NotFoundPostSerializer : KSerializer<NotFoundPost> by valueClassSerializer()

  @Serializable(with = NotFoundPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#notFoundPost")
  public value class NotFoundPost(
    public val `value`: app.bsky.feed.NotFoundPost,
  ) : ReplyRefRootUnion

  public class BlockedPostSerializer : KSerializer<BlockedPost> by valueClassSerializer()

  @Serializable(with = BlockedPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#blockedPost")
  public value class BlockedPost(
    public val `value`: app.bsky.feed.BlockedPost,
  ) : ReplyRefRootUnion
}

@Serializable
public sealed interface ReplyRefParentUnion {
  public class PostViewSerializer : KSerializer<PostView> by valueClassSerializer()

  @Serializable(with = PostViewSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#postView")
  public value class PostView(
    public val `value`: app.bsky.feed.PostView,
  ) : ReplyRefParentUnion

  public class NotFoundPostSerializer : KSerializer<NotFoundPost> by valueClassSerializer()

  @Serializable(with = NotFoundPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#notFoundPost")
  public value class NotFoundPost(
    public val `value`: app.bsky.feed.NotFoundPost,
  ) : ReplyRefParentUnion

  public class BlockedPostSerializer : KSerializer<BlockedPost> by valueClassSerializer()

  @Serializable(with = BlockedPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#blockedPost")
  public value class BlockedPost(
    public val `value`: app.bsky.feed.BlockedPost,
  ) : ReplyRefParentUnion
}

@Serializable
public data class ReplyRef(
  public val root: ReplyRefRootUnion,
  public val parent: ReplyRefParentUnion,
)
