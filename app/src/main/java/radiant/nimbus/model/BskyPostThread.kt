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
import radiant.nimbus.model.ThreadPost.BlockedPost
import radiant.nimbus.model.ThreadPost.NotFoundPost
import radiant.nimbus.model.ThreadPost.ViewablePost
import radiant.nimbus.util.mapImmutable
import radiant.nimbus.api.AtUri


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
)

@Serializable
sealed interface ThreadPost {
    @Serializable
    data class ViewablePost(
        val post: BskyPost,
        val replies: ImmutableList<ThreadPost> = persistentListOf(),
    ) : ThreadPost

    @Serializable
    data class NotFoundPost(
        val uri: AtUri? = null,
    ) :ThreadPost

    @Serializable
    data class BlockedPost(
        val uri: AtUri? = null,
    ) : ThreadPost
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


