package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.actor.ProfileViewBasic
import app.bsky.feed.*
import com.atproto.repo.StrongRef
import com.morpho.butterfly.Butterfly
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
    return BskyPostReply(
        replyRef = this.toReplyRef(),
        grandParentAuthor = this.grandParentAuthor?.toProfile()
    )
}

suspend fun PostReplyRef.hydratedReply(api: Butterfly): BskyPostReply {
    val parents = api.api.getPosts(GetPostsQuery(persistentListOf(this.parent.uri, this.root.uri)))
        .getOrNull()?.posts?.map { it.toPost() } ?: persistentListOf()
    val grandparent = if (parents.first().reply?.replyRef?.parent?.uri != null) {
        api.api.getPosts(GetPostsQuery(persistentListOf(parents.first().reply?.replyRef?.parent?.uri!!))).getOrNull()?.posts?.firstOrNull()
    } else null
    return BskyPostReply(
        rootPost = parents.firstOrNull { it.cid == this.root.cid },
        parentPost = parents.firstOrNull { it.cid == this.parent.cid },
        grandParentAuthor = this.grandParentAuthor?.toProfile() ?: grandparent?.author?.toProfile(),
    )
}