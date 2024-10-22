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
import kotlinx.serialization.Serializable


@Parcelize
@Immutable
@Serializable
data class BskyPostThread(
    val post: BskyPost,
    val parent: ThreadPost? = null,
    val replies: List<ThreadPost>,
):  Parcelable {
    val parents: List<ThreadPost> = if(parent != null) listOf(parent) + parent.parents() else listOf()

    operator fun contains(other: Any?) : Boolean {
        when(other) {
            null -> return false
            is Cid -> return other == post.cid
            is AtUri -> return other == post.uri
            is BskyPost -> return other.cid == post.cid
            else -> {

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
        val threadReplies: MutableList<ThreadPost?> = this.replies.toMutableList()
        threadReplies.fastForEachIndexed { index, reply ->
            if (reply != null && filter(reply)) {
                threadReplies[index] = null
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
            parent = parent,
            replies = threadReplies.filterNotNull()
        )
    }

    fun addReply(reply: ViewablePost): BskyPostThread {
        if(reply.uri == post.uri) return BskyPostThread(
            post = post,
            parent = if(reply.parent is ViewablePost && parent !is ViewablePost)
                reply.parent else parent,
            replies = replies.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
        )
        reply.post.reply?.parentPost?.uri ?: return this
        reply.post.reply.rootPost?.uri ?: return this
        val threadReplies = this.replies.toMutableList()
        val inParents = this.parents.any { it.uri == reply.uri }
        val inReplies = this.replies.firstOrNull { it.uri == reply.uri }
        return BskyPostThread(
            post = post,
            parent = if (!inParents) parent else if (inReplies != null) {
                if (inReplies is ViewablePost) parent?.addReply(inReplies) else parent
            } else parent,
            replies = threadReplies.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
        )
    }

    fun addReply(reply: BskyPost): BskyPostThread {
        if(reply.uri == post.uri) return BskyPostThread(
            post = post,
            parent = parent,
            replies = replies.distinctBy { it.uri }.filterNot { it.uri == reply.uri || it.uri == post.uri },
        )
        reply.reply?.parentPost?.uri ?: return this
        reply.reply.rootPost?.uri ?: return this
        val threadReplies = this.replies.toMutableList()
        val inParents = this.parents.any { it.uri == reply.uri }
        val inReplies = this.replies.any { it.uri == reply.uri }
        val inAnyParentReplies = this.parents.any {
            if(it is ViewablePost) it.replies.any { it.uri == reply.uri } else false
        }
        if(!inReplies && !inParents && !inAnyParentReplies) {
            threadReplies.add(reply.toThreadPost())
        } else if(!inParents && inAnyParentReplies) {
            parent?.addReply(reply)
        }
        val newThread = BskyPostThread(
            post = post,
            parent = parent,
            replies = threadReplies.distinctBy { it.uri },
        )
        return newThread
    }
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

    fun parents(): List<ThreadPost> {
        val parent = when(this) {
            is ViewablePost -> this.parent
            is BlockedPost -> null
            is NotFoundPost -> null
        }
        return if(parent != null) listOf(parent) + parent.parents() else listOf()
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

    fun addParentReply(reply: BskyPost): ThreadPost {
        return if(this !is ViewablePost) this
        else if(this.parent == null) this
        else if(reply.reply?.parentPost?.uri == this.parent.uri) {
            this.parent.addReply(reply)
        } else this.parent.addParentReply(reply)
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
    val entryPost = this.post.toPost()
    val newParent = parent?.toThreadPost()
    val rootPost = parent?.getRoot()
    val parentPost = if(newParent is ThreadPost.ViewablePost) newParent else null
    val grandParent = if(parentPost?.parent is ThreadPost.ViewablePost) parentPost.parent else null
    val postReply = BskyPostReply(
        rootPost = rootPost?.toPost(),
        parentPost = parentPost?.post,
        grandParentAuthor = grandParent?.post?.author,
        replyRef = entryPost.reply?.replyRef
    )
    return BskyPostThread(
        post = entryPost.copy(reply = postReply),
        parent = newParent,
        replies = replies.map { it.toThreadPost() }
    )
}

fun ThreadViewPost.toThreadPost(): ThreadPost {
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

fun ThreadViewPostParentUnion.getRoot(): ThreadViewPost? {
    return when(this) {
        is ThreadViewPostParentUnion.ThreadViewPost -> this.value.parent?.getRoot()
        is ThreadViewPostParentUnion.NotFoundPost -> null
        is ThreadViewPostParentUnion.BlockedPost -> null
    }
}


