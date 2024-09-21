package com.morpho.app.model.bluesky

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline


@Serializable
public sealed interface ThreadViewPostUnion {
    @Serializable
    @JvmInline
    @SerialName("app.bsky.feed.defs#threadViewPost")
    public value class ThreadViewPost(
        public val `value`: app.bsky.feed.ThreadViewPost,
    ) : ThreadViewPostUnion

    @Serializable
    @JvmInline
    @SerialName("app.bsky.feed.defs#notFoundPost")
    public value class NotFoundPost(
        public val `value`: app.bsky.feed.NotFoundPost,
    ) : ThreadViewPostUnion

    @Serializable
    @JvmInline
    @SerialName("app.bsky.feed.defs#blockedPost")
    public value class BlockedPost(
        public val `value`: app.bsky.feed.BlockedPost,
    ) : ThreadViewPostUnion
}
