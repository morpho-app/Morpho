package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.actor.ProfileViewBasic
import app.bsky.feed.ReplyRef
import app.bsky.feed.ReplyRefParentUnion
import app.bsky.feed.ReplyRefRootUnion
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class BskyPostReply(
    val root: BskyPost?,
    val parent: BskyPost?,
    val grandparentAuthor: Profile?
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
        grandparentAuthor = when (val grandparentAuthor = grandparentAuthor) {
            is ProfileViewBasic -> grandparentAuthor.toProfile()
            else -> null
        }
    )
}
