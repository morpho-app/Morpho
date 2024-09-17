@file:Suppress("MemberVisibilityCanBePrivate")

package com.morpho.app.model.bluesky
import androidx.compose.runtime.Immutable
import androidx.compose.ui.util.fastForEachIndexed
import app.bsky.feed.ThreadViewPost
import app.bsky.feed.ThreadViewPostParentUnion
import app.bsky.feed.ThreadViewPostReplyUnion
import com.morpho.app.model.bluesky.ThreadPost.*
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable


@Parcelize
@Immutable
@Serializable
data class BskyPostThread(
    val post: BskyPost,
    val parents: List<ThreadPost>,
    val replies: List<ThreadPost>,
):  Parcelable {
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

    fun anyMutedOrBlocked(): Boolean {
        return this.post.author.mutedByMe || this.post.author.blocking
            || this.post.author.blockedBy || this.replies.any { it.anyMutedOrBlocked() }
            || this.parents.any { it.anyMutedOrBlocked() }
    }

    fun containsWord(word: String): Boolean {
        return this.post.text.contains(word, ignoreCase = true)
            || this.replies.any { it.containsWord(word) }
            || this.parents.any { it.containsWord(word) }
    }

    fun getLabels(): List<BskyLabel> {
        return this.post.labels + this.replies.flatMap { it.getLabels() }
    }

    fun containsLabel(label: String): Boolean {
        return this.post.labels.any { it.value == label }
            || this.replies.any { it.containsLabel(label) }
            || this.parents.any { it.containsLabel(label) }
    }

    fun filterReplies(filter: (ThreadPost) -> Boolean): BskyPostThread {
        val threadReplies = this.replies.toMutableList()
        threadReplies.fastForEachIndexed { index, reply ->
            if (filter(reply)) {
                threadReplies.removeAt(index)
            } else {
                if (reply is ViewablePost) {
                    threadReplies[index] = reply.copy(
                        replies = reply.replies.filterNot { filter(it) }
                    )
                }
            }
        }
        return BskyPostThread(
            post = post,
            parents = parents,
            replies = threadReplies
        )
    }

    fun addReply(reply: ThreadPost.ViewablePost): BskyPostThread {
        if(reply.uri == post.uri) return BskyPostThread(
            post = post,
            parents = parents.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
            replies = replies.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
        )
        val parent = reply.post.reply?.parentPost?.uri ?: return this
        val root = reply.post.reply.rootPost?.uri ?: return this
        val newParents = this.parents.toMutableList()
        val threadReplies = this.replies.toMutableList()
        val inParents = this.parents.indexOfFirst {
            it.uri == parent || it.uri == root
        }
        val inReplies = this.replies.indexOfFirst {
            it.uri == parent || it.uri == root
        }
        if (inParents != -1) {
            val replyParent = parents[inParents]
            replyParent.addReply(reply)
            newParents[inParents] = replyParent
        } else if (inReplies != -1) {
            val replyParent = threadReplies[inReplies]
            replyParent.addReply(reply)
            threadReplies[inReplies] = replyParent
        }
        return BskyPostThread(
            post = post,
            parents = newParents.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
            replies = threadReplies.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
        )
    }

    fun addReply(reply: BskyPost): BskyPostThread {
        if(reply.uri == post.uri) return BskyPostThread(
            post = post,
            parents = parents.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
            replies = replies.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
        )
        val parent = reply.reply?.parentPost?.uri ?: return this
        val root = reply.reply.rootPost?.uri ?: return this
        val newParents = this.parents.toMutableList()
        val threadReplies = this.replies.toMutableList()
        val inParents = this.parents.indexOfFirst {
            it.uri == parent || it.uri == root
        }
        val inReplies = this.replies.indexOfFirst {
            it.uri == parent
        }
        if (inParents != -1) {
            val replyParent = parents[inParents]
            replyParent.addReply(reply)
            newParents[inParents] = replyParent
        } else if (inReplies != -1) {
            val replyParent = threadReplies[inReplies]
            replyParent.addReply(reply)
            threadReplies[inReplies] = replyParent
        }
        return BskyPostThread(
            post = post,
            parents = newParents.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
            replies = threadReplies.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
        )
    }
}

fun List<ThreadPost>.inParentOrder(): List<ThreadPost> {
    val newList = this.toMutableList()
    this.forEachIndexed { index, threadPost ->
        when(threadPost) {
            is ViewablePost -> {
                val parentUri = threadPost.post.reply?.replyRef?.parent?.uri
                if (threadPost.post.reply == null) {
                    newList.add(0, threadPost)
                    return@forEachIndexed
                }
                val parentIndex = newList.indexOfFirst { it.uri == parentUri }
                if (parentIndex != -1 && parentIndex != index - 1) {
                    newList.add(parentIndex+1, threadPost)
                    return@forEachIndexed
                }
            }
            else -> return@forEachIndexed
        }
    }
    return newList.distinctBy { it.uri }
}

@Parcelize
@Immutable
@Serializable
sealed interface ThreadPost:Parcelable {
    val uri: AtUri?

    @Immutable
    @Serializable
    data class ViewablePost(
        val post: BskyPost,
        val parent: ThreadPost? = null,
        val replies: List<ThreadPost> = persistentListOf(),
    ) : ThreadPost {
        override val uri: AtUri
            get() = post.uri
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
        override val uri: AtUri? = null,
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
        override val uri: AtUri? = null,
    ) : ThreadPost {
        override fun equals(other: Any?) : Boolean {
            if (other is AtUri) return uri == other
            return this.hashCode() == other.hashCode()
        }

        override fun hashCode(): Int {
            return uri?.hashCode() ?: 0
        }
    }

    fun anyMutedOrBlocked(): Boolean {
        return when(this) {
            is ViewablePost -> this.post.author.mutedByMe || this.post.author.blocking
                    || this.post.author.blockedBy || this.replies.any { it.anyMutedOrBlocked() }

            is BlockedPost -> true
            is NotFoundPost -> true
        }
    }

    fun containsLabel(label: String): Boolean {
        return when(this) {
            is ViewablePost -> this.post.labels.any { it.value == label }
                    || this.replies.any { it.containsLabel(label) }
            is BlockedPost -> false
            is NotFoundPost -> false
        }
    }

    fun getLabels(): List<BskyLabel> {
        return when(this) {
            is ViewablePost -> this.post.labels + this.replies.flatMap { it.getLabels() }
            is BlockedPost -> listOf()
            is NotFoundPost -> listOf()
        }
    }

    fun containsWord(word: String): Boolean {
        return when(this) {
            is ViewablePost -> this.post.text.contains(word, ignoreCase = true)
                    || this.replies.any { it.containsWord(word) }
            is BlockedPost -> false
            is NotFoundPost -> false
        }
    }


    fun addReply(reply: BskyPost): ThreadPost {
        return addReply(ViewablePost(reply))
    }

    fun addReply(reply: ViewablePost): ThreadPost {
        return when(this) {
            is ViewablePost -> ViewablePost(post, parent, (replies + reply).distinctBy { it.uri })
            is BlockedPost -> BlockedPost(uri)
            is NotFoundPost -> NotFoundPost(uri)
        }
    }

    fun hasReplies(): Boolean {
        return when(this) {
            is ViewablePost -> replies.isNotEmpty()
            else -> false
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
            replies = replies.mapImmutable { it.toThreadPost() }
        )
    } else {
        val rootPost = parents.last().toPost()
        val entryPost = this.post.toPost(BskyPostReply(parents.first().toPost(), rootPost, parents.first().toPost().reply?.parentPost?.author), null)
        return BskyPostThread(
            post = entryPost,
            parents = parents.mapIndexed { index, post ->
                post.toThreadPost(
                    if(index == parents.lastIndex) {
                        post.toPost()
                    } else {
                        parents[index + 1].toPost()
                    },
                )
            }.reversed().toImmutableList(),
            replies = replies.mapImmutable { reply -> reply.toThreadPost() },
        )
    }
}

fun ThreadViewPost.toThreadPost( root: BskyPost): ThreadPost {
    val post = post.toPost(null, null)
    return ViewablePost(
        post = post,
        parent = parent?.toThreadPost(),
        replies = replies.mapImmutable { it.toThreadPost() }
    )
}

fun ThreadViewPostReplyUnion.toThreadPost(): ThreadPost = when (this) {
    is ThreadViewPostReplyUnion.ThreadViewPost -> {
        val post = value.post.toPost(null, null)
        ViewablePost(
            post = post,
            parent = value.parent?.toThreadPost(),
            replies = value.replies.mapImmutable { it.toThreadPost() }
        )
    }
    is ThreadViewPostReplyUnion.NotFoundPost -> NotFoundPost(value.uri)
    is ThreadViewPostReplyUnion.BlockedPost -> BlockedPost(value.uri)
}

fun ThreadViewPostParentUnion.toThreadPost(): ThreadPost = when (this) {
    is ThreadViewPostParentUnion.ThreadViewPost -> {
        val post = value.post.toPost(null, null)
        ViewablePost(
            post = post,
            parent = value.parent?.toThreadPost(),
            replies = value.replies.mapImmutable { it.toThreadPost() }
        )
    }
    is ThreadViewPostParentUnion.NotFoundPost -> NotFoundPost(value.uri)
    is ThreadViewPostParentUnion.BlockedPost -> BlockedPost(value.uri)
}




