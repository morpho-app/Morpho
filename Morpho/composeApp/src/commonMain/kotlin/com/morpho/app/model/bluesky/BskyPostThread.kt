@file:Suppress("MemberVisibilityCanBePrivate")

package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable

import app.bsky.feed.ThreadViewPost
import app.bsky.feed.ThreadViewPostParentUnion
import app.bsky.feed.ThreadViewPostReplyUnion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.app.model.bluesky.ThreadPost.BlockedPost
import com.morpho.app.model.bluesky.ThreadPost.NotFoundPost
import com.morpho.app.model.bluesky.ThreadPost.ViewablePost
import com.morpho.app.util.mapImmutable
import kotlinx.collections.immutable.toImmutableList


@Immutable
@Serializable
data class BskyPostThread(
    val post: BskyPost,
    private val _parents: List<ThreadPost>,
    val replies: ImmutableList<ThreadPost>,
) {
    val parents = _parents.toImmutableList()
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
    return BskyPostThread(
        post = toPost(),
        _parents = generateSequence(parent) { parentPost ->
            when (parentPost) {
                is ThreadViewPostParentUnion.BlockedPost -> null
                is ThreadViewPostParentUnion.NotFoundPost -> null
                is ThreadViewPostParentUnion.ThreadViewPost -> parentPost.value.parent
            }
        }
            .map { it.toThreadPost() }
            .toList()
            .reversed().toImmutableList(),
        replies = replies.mapImmutable { reply -> reply.toThreadPost() },
    )
}

fun ThreadViewPostParentUnion.toThreadPost(): ThreadPost = when (this) {
    is ThreadViewPostParentUnion.ThreadViewPost -> ViewablePost(
        post = value.toPost(),
        replies = value.replies.mapImmutable { it.toThreadPost() }
    )
    is ThreadViewPostParentUnion.NotFoundPost -> NotFoundPost(value.uri)
    is ThreadViewPostParentUnion.BlockedPost -> BlockedPost(value.uri)
}

fun ThreadViewPostReplyUnion.toThreadPost(): ThreadPost = when (this) {
    is ThreadViewPostReplyUnion.ThreadViewPost -> ViewablePost(
        post = value.toPost(),
        replies = value.replies.mapImmutable { it.toThreadPost() },
    )
    is ThreadViewPostReplyUnion.NotFoundPost -> NotFoundPost(value.uri)
    is ThreadViewPostReplyUnion.BlockedPost -> BlockedPost(value.uri)
}


