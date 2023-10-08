@file:Suppress("MemberVisibilityCanBePrivate")

package radiant.nimbus.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.bsky.feed.ThreadViewPost
import app.bsky.feed.ThreadViewPostParentUnion
import app.bsky.feed.ThreadViewPostReplieUnion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Cid
import radiant.nimbus.model.ThreadPost.BlockedPost
import radiant.nimbus.model.ThreadPost.NotFoundPost
import radiant.nimbus.model.ThreadPost.ViewablePost
import radiant.nimbus.util.mapImmutable


@Entity(tableName = "threads")
data class BskyDbThread(
    @PrimaryKey val startUri: String,
    val parentIds: List<Long> = mutableListOf(),
    val replyIds: List<Long> = mutableListOf(),
)

@Serializable
data class BskyPostThread(
    val post: BskyPost,
    val parents: List<ThreadPost>,
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

@Serializable
sealed interface ThreadPost {
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

    @Serializable
    data class NotFoundPost(
        val uri: AtUri? = null,
    ) :ThreadPost {
        override fun equals(other: Any?) : Boolean {
            if (other is AtUri) return uri == other
            return this.hashCode() == other.hashCode()
        }

        override fun hashCode(): Int {
            return uri?.hashCode() ?: 0
        }
    }

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
        post = post.toPost(),
        parents = generateSequence(parent) { parentPost ->
            when (parentPost) {
                is ThreadViewPostParentUnion.BlockedPost -> null
                is ThreadViewPostParentUnion.NotFoundPost -> null
                is ThreadViewPostParentUnion.ThreadViewPost -> parentPost.value.parent
            }
        }
            .map { it.toThreadPost() }
            .toList()
            .reversed(),
        replies = replies.mapImmutable { reply -> reply.toThreadPost() },
    )
}

fun ThreadViewPostParentUnion.toThreadPost(): ThreadPost = when (this) {
    is ThreadViewPostParentUnion.ThreadViewPost -> ViewablePost(
        post = value.post.toPost(),
        replies = value.replies.mapImmutable { it.toThreadPost() }
    )
    is ThreadViewPostParentUnion.NotFoundPost -> NotFoundPost(value.uri)
    is ThreadViewPostParentUnion.BlockedPost -> BlockedPost(value.uri)
}

fun ThreadViewPostReplieUnion.toThreadPost(): ThreadPost = when (this) {
    is ThreadViewPostReplieUnion.ThreadViewPost -> ViewablePost(
        post = value.post.toPost(),
        replies = value.replies.mapImmutable { it.toThreadPost() },
    )
    is ThreadViewPostReplieUnion.NotFoundPost -> NotFoundPost(value.uri)
    is ThreadViewPostReplieUnion.BlockedPost -> BlockedPost(value.uri)
}


