@file:Suppress("MemberVisibilityCanBePrivate")

package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.feed.ThreadViewPost
import app.bsky.feed.ThreadViewPostParentUnion
import app.bsky.feed.ThreadViewPostReplyUnion
import com.morpho.app.model.bluesky.ThreadPost.*
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable


@Immutable
@Serializable
data class BskyPostThread(
    val post: BskyPost,
    val parents: ImmutableList<ThreadPost>,
    val replies: ImmutableList<ThreadPost>,
) {
    operator fun contains(other: Any?) : Boolean {
        when(other) {
            null -> return false
            is Cid -> return other == post.cid
            is AtUri -> return other == post.uri
            is BskyPost -> return other.cid == post.cid
            else -> {
                parents.map {
                    return when(it) {
                        is BlockedPost -> false
                        is NotFoundPost -> false
                        is ViewablePost -> it.contains(other)
                    }
                }
                replies.map {
                    return when(it) {
                        is BlockedPost -> false
                        is NotFoundPost -> false
                        is ViewablePost -> it.contains(other)
                    }
                }
            }
        }
        return false
    }
}


@Immutable
@Serializable
sealed interface ThreadPost {

    @Immutable
    @Serializable
    data class ViewablePost(
        val post: BskyPost,
        val replies: ImmutableList<ThreadPost> = persistentListOf(),
    ) : ThreadPost {
        override fun equals(other: Any?) : Boolean {
            return when(other) {
                null -> false
                is Cid -> other == post.cid
                is BskyPost -> other.cid == post.cid
                else -> other.hashCode() == this.hashCode()
            }
        }

        operator fun contains(other: Any?) : Boolean {
            when(other) {
                is Cid -> {
                    if (other == post.cid) {
                        return true
                    } else {
                        replies.mapImmutable {
                            return when (it) {
                                is BlockedPost -> false
                                is NotFoundPost -> false
                                is ViewablePost -> it.contains(other)
                            }
                        }
                    }
                }
                is AtUri -> {
                    if (other == post.uri) {
                        return true
                    } else {
                        replies.mapImmutable {
                            return when (it) {
                                is BlockedPost -> false
                                is NotFoundPost -> false
                                is ViewablePost -> it.contains(other)
                            }
                        }
                    }
                }
                is BskyPost -> {
                    if (other.cid == post.cid) {
                        return true
                    } else {
                        replies.mapImmutable {
                            return when (it) {
                                is BlockedPost -> false
                                is NotFoundPost -> false
                                is ViewablePost -> it.contains(other)
                            }
                        }
                    }
                }
                else -> {
                    return other.hashCode() == this.hashCode()
                }
            }
            return false
        }

        override fun hashCode(): Int {
            var result = post.hashCode()
            result = 31 * replies.fold(result) { i: Int, threadPost: ThreadPost ->
                i + when(threadPost) {
                    is BlockedPost -> threadPost.hashCode()
                    is NotFoundPost -> threadPost.hashCode()
                    is ViewablePost -> threadPost.post.cid.hashCode()
                }
            }
            return result
        }
    }

    @Immutable
    @Serializable
    data class NotFoundPost(
        val uri: AtUri? = null,
    ) : ThreadPost {
        override fun equals(other: Any?) : Boolean {
            if (other is AtUri) return uri == other
            return this.hashCode() == other.hashCode()
        }

        override fun hashCode(): Int {
            return uri?.hashCode() ?: 0
        }
    }

    @Immutable
    @Serializable
    data class BlockedPost(
        val uri: AtUri? = null,
    ) : ThreadPost {
        override fun equals(other: Any?) : Boolean {
            if (other is AtUri) return uri == other
            return this.hashCode() == other.hashCode()
        }

        override fun hashCode(): Int {
            return uri?.hashCode() ?: 0
        }
    }


}

fun ThreadViewPost.toThread(): BskyPostThread {
    val parents = when(parent) {
        is ThreadViewPostParentUnion.ThreadViewPost -> {
            (parent as ThreadViewPostParentUnion.ThreadViewPost).value.findParentChain()
        }
        else -> persistentListOf()
    }
    if (parents.isEmpty()) {
        return BskyPostThread(
            post = post.toPost(),
            parents = persistentListOf(),
            replies = replies.mapImmutable { it.toThreadPost(post.toPost(), post.toPost()) }
        )
    } else {
        val rootPost = parents.last().toPost()
        val entryPost = this.post.toPost(BskyPostReply(parents.first().toPost(), rootPost), null)
        return BskyPostThread(
            post = entryPost,
            parents = parents.mapIndexed { index, post ->
                post.toThreadPost(
                    if(index == parents.lastIndex) {
                        post.toPost()
                    } else {
                        parents[index + 1].toPost()
                    },
                    rootPost
                )
            }.reversed().toImmutableList(),
            replies = replies.mapImmutable { reply -> reply.toThreadPost(entryPost, rootPost) },
        )
    }
}

fun ThreadViewPost.toThreadPost(parent: BskyPost, root: BskyPost): ThreadPost {
    val post = post.toPost(BskyPostReply(root, parent), null)
    return ViewablePost(
        post = post,
        replies = replies.mapImmutable { it.toThreadPost(post, root) }
    )
}

fun ThreadViewPostReplyUnion.toThreadPost(parent: BskyPost, root: BskyPost): ThreadPost = when (this) {
    is ThreadViewPostReplyUnion.ThreadViewPost -> {
        val post = value.post.toPost(BskyPostReply(root, parent), null)
        ViewablePost(
            post = post,
            replies = value.replies.mapImmutable { it.toThreadPost(post, root) }
        )
    }
    is ThreadViewPostReplyUnion.NotFoundPost -> NotFoundPost(value.uri)
    is ThreadViewPostReplyUnion.BlockedPost -> BlockedPost(value.uri)
}


