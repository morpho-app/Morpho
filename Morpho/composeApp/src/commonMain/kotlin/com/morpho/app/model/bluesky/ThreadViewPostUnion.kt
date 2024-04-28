package com.morpho.app.model.bluesky

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import com.morpho.butterfly.valueClassSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline


@kotlinx.serialization.Serializable
public sealed interface ThreadViewPostUnion {
    public class ThreadViewPostSerializer : KSerializer<ThreadViewPost> by valueClassSerializer(
        serialName = "app.bsky.feed.defs#threadViewPost",
        constructor = ThreadViewPostUnion::ThreadViewPost,
        valueProvider = ThreadViewPost::value,
        valueSerializerProvider = { app.bsky.feed.ThreadViewPost.serializer() },
    )
    @Serializable(with = ThreadViewPostSerializer::class)
    @JvmInline
    @SerialName("app.bsky.feed.defs#threadViewPost")
    public value class ThreadViewPost(
        public val `value`: app.bsky.feed.ThreadViewPost,
    ) : ThreadViewPostUnion

    public class NotFoundPostSerializer : KSerializer<NotFoundPost> by valueClassSerializer(
        serialName = "app.bsky.feed.defs#notFoundPost",
        constructor = ThreadViewPostUnion::NotFoundPost,
        valueProvider = NotFoundPost::value,
        valueSerializerProvider = { app.bsky.feed.NotFoundPost.serializer() },
    )

    @Serializable(with = NotFoundPostSerializer::class)
    @JvmInline
    @SerialName("app.bsky.feed.defs#notFoundPost")
    public value class NotFoundPost(
        public val `value`: app.bsky.feed.NotFoundPost,
    ) : ThreadViewPostUnion

    public class BlockedPostSerializer : KSerializer<BlockedPost> by valueClassSerializer(
        serialName = "app.bsky.feed.defs#blockedPost",
        constructor = ThreadViewPostUnion::BlockedPost,
        valueProvider = BlockedPost::value,
        valueSerializerProvider = { app.bsky.feed.BlockedPost.serializer() },
    )
    @Serializable(with = BlockedPostSerializer::class)
    @JvmInline
    @SerialName("app.bsky.feed.defs#blockedPost")
    public value class BlockedPost(
        public val `value`: app.bsky.feed.BlockedPost,
    ) : ThreadViewPostUnion
}
