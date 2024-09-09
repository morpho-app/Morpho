package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.actor.ProfileViewBasic
import app.bsky.feed.PostReplyRef
import app.bsky.feed.ReplyRef
import app.bsky.feed.ReplyRefParentUnion
import app.bsky.feed.ReplyRefRootUnion
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class BskyPostReply(
    val root: BskyPost? = null,
    val parent: BskyPost? = null,
    val grandParentAuthor: Profile? = null,
    val replyRef: PostReplyRef? = null
)

fun ReplyRef.toReply(): BskyPostReply {
    return BskyPostReply(
        root = when (val root = root) {
            is ReplyRefRootUnion.BlockedPost -> null
            is ReplyRefRootUnion.NotFoundPost -> null
            is ReplyRefRootUnion.PostView -> root.value.toPost()
        },
        parent = when (val parent = parent) {
            is ReplyRefParentUnion.BlockedPost -> null
            is ReplyRefParentUnion.NotFoundPost -> null
            is ReplyRefParentUnion.PostView -> parent.value.toPost()
        },
        grandParentAuthor = when (val grandparentAuthor = grandparentAuthor) {
            is ProfileViewBasic -> grandparentAuthor.toProfile()
            else -> null
        }
    )
}

fun PostReplyRef.toReply(): BskyPostReply {
    return BskyPostReply(
        replyRef = this,
        grandParentAuthor = this.grandParentAuthor?.toProfile()
    )
}