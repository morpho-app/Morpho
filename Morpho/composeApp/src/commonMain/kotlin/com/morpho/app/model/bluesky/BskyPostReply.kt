package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.actor.ProfileViewBasic
import app.bsky.feed.GetPostsQuery
import app.bsky.feed.PostReplyRef
import app.bsky.feed.ReplyRef
import app.bsky.feed.ReplyRefParentUnion
import app.bsky.feed.ReplyRefRootUnion
import com.atproto.repo.StrongRef
import com.morpho.app.data.MorphoAgent
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Parcelize
@Immutable
@Serializable
data class BskyPostReply(
    val rootPost: BskyPost? = null,
    val parentPost: BskyPost? = null,
    val grandParentAuthor: Profile? = null,
    val replyRef: BskyPostReplyRef? = null
): Parcelable

fun ReplyRef.toReply(): BskyPostReply {
    return BskyPostReply(
        rootPost = when (val root = root) {
            is ReplyRefRootUnion.BlockedPost -> null
            is ReplyRefRootUnion.NotFoundPost -> null
            is ReplyRefRootUnion.PostView -> root.value.toPost()
        },
        parentPost = when (val parent = parent) {
            is ReplyRefParentUnion.BlockedPost -> null
            is ReplyRefParentUnion.NotFoundPost -> null
            is ReplyRefParentUnion.PostView -> parent.value.toPost()
        },
        grandParentAuthor = when (val grandParentAuthor = this.grandparentAuthor) {
            is ProfileViewBasic -> grandParentAuthor.toProfile()
            else -> null
        }
    )
}

@Parcelize
@Immutable
@Serializable
public data class BskyPostReplyRef(
    public val root: StrongRef,
    public val parent: StrongRef,
    public val grandParentAuthor: Profile? = null,
): Parcelable

fun PostReplyRef.toReplyRef(): BskyPostReplyRef {
    return BskyPostReplyRef(
        root = this.root,
        parent = this.parent,
        grandParentAuthor = this.grandParentAuthor?.toProfile()
    )
}

fun PostReplyRef.toReply(): BskyPostReply {
    val replyRef = this.toReplyRef()
    return BskyPostReply(
        replyRef = replyRef,
        grandParentAuthor = replyRef.grandParentAuthor
    )
}

suspend fun PostReplyRef.hydratedReply(agent: MorphoAgent): BskyPostReply {
    val parents = agent.api.getPosts(GetPostsQuery(persistentListOf(this.parent.uri, this.root.uri)))
        .getOrNull()?.posts?.map { it.toPost() } ?: persistentListOf()
    val grandparent = if (parents.first().reply?.replyRef?.parent?.uri != null) {
        agent.api.getPosts(GetPostsQuery(persistentListOf(parents.first().reply?.replyRef?.parent?.uri!!))).getOrNull()?.posts?.firstOrNull()
    } else null
    return BskyPostReply(
        rootPost = parents.firstOrNull { it.cid == this.root.cid },
        parentPost = parents.firstOrNull { it.cid == this.parent.cid },
        grandParentAuthor = this.grandParentAuthor?.toProfile() ?: grandparent?.author?.toProfile(),
    )
}