package app.bsky.feed

import kotlin.Any
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.ReadOnlyList
import radiant.nimbus.api.runtime.valueClassSerializer

@Serializable
public sealed interface GetPostThreadResponseThreadUnion {
  public class ThreadViewPostSerializer : KSerializer<ThreadViewPost> by valueClassSerializer()

  @Serializable(with = ThreadViewPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#threadViewPost")
  public value class ThreadViewPost(
    public val `value`: app.bsky.feed.ThreadViewPost,
  ) : GetPostThreadResponseThreadUnion

  public class NotFoundPostSerializer : KSerializer<NotFoundPost> by valueClassSerializer()

  @Serializable(with = NotFoundPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#notFoundPost")
  public value class NotFoundPost(
    public val `value`: app.bsky.feed.NotFoundPost,
  ) : GetPostThreadResponseThreadUnion

  public class BlockedPostSerializer : KSerializer<BlockedPost> by valueClassSerializer()

  @Serializable(with = BlockedPostSerializer::class)
  @JvmInline
  @SerialName("app.bsky.feed.defs#blockedPost")
  public value class BlockedPost(
    public val `value`: app.bsky.feed.BlockedPost,
  ) : GetPostThreadResponseThreadUnion
}

@Serializable
public data class GetPostThreadQueryParams(
  public val uri: AtUri,
  public val depth: Long? = 6,
  public val parentHeight: Long? = 80,
) {
  init {
    require(depth == null || depth >= 0) {
      "depth must be >= 0, but was $depth"
    }
    require(depth == null || depth <= 1_000) {
      "depth must be <= 1_000, but was $depth"
    }
    require(parentHeight == null || parentHeight >= 0) {
      "parentHeight must be >= 0, but was $parentHeight"
    }
    require(parentHeight == null || parentHeight <= 1_000) {
      "parentHeight must be <= 1_000, but was $parentHeight"
    }
  }

  public fun asList(): ReadOnlyList<Pair<String, Any?>> = buildList {
    add("uri" to uri)
    add("depth" to depth)
    add("parentHeight" to parentHeight)
  }.toImmutableList()
}

@Serializable
public data class GetPostThreadResponse(
  public val thread: GetPostThreadResponseThreadUnion,
)
