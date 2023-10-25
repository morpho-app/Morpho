package radiant.nimbus.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import radiant.nimbus.api.runtime.valueClassSerializer


@kotlinx.serialization.Serializable
public sealed interface ThreadViewPostUnion {
    public class ThreadViewPostSerializer : KSerializer<ThreadViewPost> by valueClassSerializer()

    @kotlinx.serialization.Serializable(with = ThreadViewPostSerializer::class)
    @JvmInline
    @SerialName("app.bsky.feed.defs#threadViewPost")
    public value class ThreadViewPost(
        public val `value`: app.bsky.feed.ThreadViewPost,
    ) : ThreadViewPostUnion

    public class NotFoundPostSerializer : KSerializer<NotFoundPost> by valueClassSerializer()

    @kotlinx.serialization.Serializable(with = NotFoundPostSerializer::class)
    @JvmInline
    @SerialName("app.bsky.feed.defs#notFoundPost")
    public value class NotFoundPost(
        public val `value`: app.bsky.feed.NotFoundPost,
    ) : ThreadViewPostUnion

    public class BlockedPostSerializer : KSerializer<BlockedPost> by valueClassSerializer()

    @kotlinx.serialization.Serializable(with = BlockedPostSerializer::class)
    @JvmInline
    @SerialName("app.bsky.feed.defs#blockedPost")
    public value class BlockedPost(
        public val `value`: app.bsky.feed.BlockedPost,
    ) : ThreadViewPostUnion
}
