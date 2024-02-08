package app.bsky.feed

import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import morpho.app.api.model.ReadOnlyList
import morpho.app.api.runtime.valueClassSerializer

@Serializable
public sealed interface ThreadViewPostParentUnion {
  public class ThreadViewPostSerializer : KSerializer<ThreadViewPost> by valueClassSerializer()

  @Serializable(with = ThreadViewPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#threadViewPost")
  public value class ThreadViewPost(
    public val `value`: app.bsky.feed.ThreadViewPost,
  ) : ThreadViewPostParentUnion

  public class NotFoundPostSerializer : KSerializer<NotFoundPost> by valueClassSerializer()

  @Serializable(with = NotFoundPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#notFoundPost")
  public value class NotFoundPost(
    public val `value`: app.bsky.feed.NotFoundPost,
  ) : ThreadViewPostParentUnion

  public class BlockedPostSerializer : KSerializer<BlockedPost> by valueClassSerializer()

  @Serializable(with = BlockedPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#blockedPost")
  public value class BlockedPost(
    public val `value`: app.bsky.feed.BlockedPost,
  ) : ThreadViewPostParentUnion
}

@Serializable
public sealed interface ThreadViewPostReplieUnion {
  public class ThreadViewPostSerializer : KSerializer<ThreadViewPost> by valueClassSerializer()

  @Serializable(with = ThreadViewPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#threadViewPost")
  public value class ThreadViewPost(
    public val `value`: app.bsky.feed.ThreadViewPost,
  ) : ThreadViewPostReplieUnion

  public class NotFoundPostSerializer : KSerializer<NotFoundPost> by valueClassSerializer()

  @Serializable(with = NotFoundPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#notFoundPost")
  public value class NotFoundPost(
    public val `value`: app.bsky.feed.NotFoundPost,
  ) : ThreadViewPostReplieUnion

  public class BlockedPostSerializer : KSerializer<BlockedPost> by valueClassSerializer()

  @Serializable(with = BlockedPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#blockedPost")
  public value class BlockedPost(
    public val `value`: app.bsky.feed.BlockedPost,
  ) : ThreadViewPostReplieUnion
}

@Serializable
public data class ThreadViewPost(
  public val post: PostView,
  public val parent: ThreadViewPostParentUnion? = null,
  public val replies: ReadOnlyList<ThreadViewPostReplieUnion> = persistentListOf(),
  public val viewer: ViewerThreadState? = null,
)
