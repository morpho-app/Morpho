package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.actor.Visibility
import app.bsky.feed.*
import com.morpho.app.CommonParcelize
import com.morpho.app.util.deserialize
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.InterpretedLabelDefinition
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Serializable


/**
 * Type union for different types of data items that can be displayed in the app.
 * Can use interface directly or use subclasses for more specific types where needed.
 * Would like to figure out how to specify only a subset of types are used in a given context.
 * This would help keep "when" statements from scenario where we want
 *      e.g. PostItems and ThreadItems from needing to handle all possible subtypes.
 */
@Parcelize
@Immutable
@Serializable
@CommonParcelize
sealed interface MorphoDataItem: Parcelable {

    @Immutable
    @Serializable
    @CommonParcelize
    sealed interface FeedItem: MorphoDataItem {

        fun getAuthors(): AuthorContext? {
            return when(this) {
                is Post -> {
                    AuthorContext(
                        author = post.author,
                        parentAuthor = post.reply?.parentPost?.author,
                        grandParentAuthor = post.reply?.parentPost?.reply?.parentPost?.author,
                        rootAuthor = post.reply?.rootPost?.author,
                    )
                }
                is Thread -> {
                    AuthorContext(
                        author = thread.post.author,
                        parentAuthor = thread.post.reply?.parentPost?.author,
                        grandParentAuthor = thread.post.reply?.parentPost?.reply?.parentPost?.author,
                        rootAuthor = thread.post.reply?.rootPost?.author,
                    )
                }
            }
        }

        val key: String
            get() = when(this) {
                is Post -> {
                    when(reason) {
                        is BskyPostReason.BskyPostRepost -> "post_${post.uri}_${reason.indexedAt}_${post.indexedAt}"
                        is BskyPostReason.BskyPostFeedPost -> "post_${post.uri}_${reason.repost}_${post.indexedAt}"
                        else -> "post_${post.uri}_${post.reason?.hashCode()?:0}_${post.indexedAt}"
                    }
                }
                is Thread -> {
                    when(reason) {
                        is BskyPostReason.BskyPostRepost -> "thread_${thread.post.uri}_${reason.indexedAt}_${thread.post.indexedAt}"
                        is BskyPostReason.BskyPostFeedPost -> "thread_${thread.post.uri}_${reason.repost}_${thread.post.indexedAt}"
                        else -> "thread_${thread.post.uri}_${thread.post.indexedAt}"
                    }
                }
            }

        val rootUri: AtUri
            get() = when(this) {
                is Post -> post.reply?.replyRef?.root?.uri ?: post.uri
                is Thread -> if(thread.post.reply != null) {
                    thread.post.reply.replyRef?.root?.uri ?: thread.post.uri
                } else thread.post.uri
            }

        val rootAccessiblePost: BskyPost
            get() = when(this) {
                is Post -> post.reply?.rootPost ?: post
                is Thread -> if(thread.post.reply != null) {
                    if(thread.post.reply.rootPost != null) {
                        thread.post.reply.rootPost
                    } else {
                        val parent = thread.parents.firstOrNull {
                            when(it) {
                                is ThreadPost.ViewablePost -> true
                                else -> false
                            }
                        }
                        when(parent) {
                            is ThreadPost.ViewablePost -> parent.post
                            else -> thread.post
                        }
                    }
                } else thread.post
            }

        val parentAuthor: Profile?
            get() = when(this) {
                is Post -> post.reply?.parentPost?.author
                is Thread -> thread.post.reply?.parentPost?.author
            }
        val isQuotePost: Boolean
            get() = when(this) {
                is Post -> when(post.feature) {
                    is BskyPostFeature.ExternalFeature -> false
                    is BskyPostFeature.ImagesFeature -> false
                    is BskyPostFeature.MediaRecordFeature -> true
                    is BskyPostFeature.RecordFeature -> true
                    is BskyPostFeature.UnknownEmbed -> false
                    is BskyPostFeature.VideoFeature -> false
                    null -> false
                }
                is Thread -> false
            }

        val isReply: Boolean
            get() = when(this) {
                is Post -> post.reply != null
                is Thread -> thread.post.reply != null
            }

        val isRepost: Boolean
            get() = when(this) {
                is Post -> post.reason is BskyPostReason.BskyPostRepost
                is Thread -> thread.post.reason is BskyPostReason.BskyPostRepost
            }

        val likeCount: Long
            get() = when(this) {
                is Post -> post.likeCount
                is Thread -> thread.post.likeCount
            }

        companion object {
            fun fromFeedViewPost(feedPost: FeedViewPost): FeedItem {
                val items = mutableListOf<PostView>()
                val reason = feedPost.reason
                val post = feedPost.post
                val reply = feedPost.reply
                var isIncompleteThread = false
                var isOrphan = false
                if (reply == null) {
                    val newPost = post.toPost()
                    return Post(newPost, newPost.reason, isOrphan = isOrphan)
                }
                if (reason != null) {
                    return Post(post.toPost(reply.toReply(), reason.toReason()), isOrphan = isOrphan)
                }

                val rootUri = reply.root.getRootStatus()?.second ?: post.uri
                val rootStatus = reply.root.getRootStatus()?.first ?: PostStatus.NotFound
                val root = reply.root.postView()
                val parent = reply.parent.postView()
                items.add(feedPost.post)
                val grandparent = if(rootStatus == PostStatus.Viewable && when(reply.parent) {
                        is ReplyRefParentUnion.BlockedPost -> false
                        is ReplyRefParentUnion.NotFoundPost -> false
                        is ReplyRefParentUnion.PostView -> {
                            val parentRef = reply.parent as ReplyRefParentUnion.PostView
                            val parentPost = try {
                                app.bsky.feed.Post.serializer().deserialize(parentRef.value.record)
                            } catch (e: Exception) {
                                null
                            }
                            parentPost?.reply?.parent?.uri == rootUri
                        }
                    }) { root } else null

                if(parent != null) items.add(0, parent)
                if (grandparent == null) isOrphan = true
                if (rootStatus != PostStatus.Viewable) {
                    return Post(post.toPost(reply.toReply(), feedPost.reason?.toReason()), null, isOrphan = true)
                }
                if (rootUri == parent?.uri) {
                    return if (items.size == 1) {
                        Post(post.toPost(reply.toReply(),  feedPost.reason?.toReason()), null, isOrphan = isOrphan)
                    } else {
                        Thread(
                            BskyPostThread(
                                post = post.toPost(reply.toReply(), null),

                                replies = listOf()
                            ),
                            null,
                            isIncompleteThread = isIncompleteThread,
                        )
                    }
                }
                if(root != null) items.add(0, root)
                if (grandparent != null) {
                    items.add(0, grandparent)
                    isIncompleteThread = true
                }
                return if (items.size == 1) {
                    Post(post.toPost(reply.toReply(),  feedPost.reason?.toReason()),  feedPost.reason?.toReason(), isOrphan = isOrphan)
                } else {

                    val thread = BskyPostThread(
                        post = post.toPost(reply.toReply(), null),
                        parent = parent?.toThreadPost(items),
                        replies = listOf()
                    )

                    Thread(
                        thread,
                        null,
                        isIncompleteThread = isIncompleteThread,
                    )
                }
            }
        }
    }

    @Immutable
    @Serializable
    @CommonParcelize
    data class Post(
        val post: BskyPost,
        val reason: BskyPostReason? = post.reason,
        val isOrphan: Boolean = false,
    ): FeedItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class Thread(
        val thread: BskyPostThread,
        val reason: BskyPostReason? = null,
        val isIncompleteThread: Boolean = false,
    ): FeedItem {
        fun addReply(reply: BskyPost): Thread {
            return this.copy(thread = thread.addReply(reply))
        }

        fun addReply(reply: ThreadPost.ViewablePost): Thread {
            return this.copy(thread = thread.addReply(reply))
        }

    }

    @Immutable
    @Serializable
    @CommonParcelize
    data class FeedInfo(
        val feed: FeedGenerator,
    ): MorphoDataItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class ProfileItem(
        val profile: DetailedProfile,
    ): MorphoDataItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class ListInfo(
        val list: BskyList,
    ): MorphoDataItem


    @Immutable
    @Serializable
    @CommonParcelize
    data class ModLabel(
        val label: InterpretedLabelDefinition,
        val setting: Visibility,
    ): MorphoDataItem


    fun containsUri(uri: AtUri): Boolean {
        return when(this) {
            is Post -> post.uri == uri
            is Thread -> {
                thread.post.uri == uri || thread.parents.any { parent ->
                    when(parent) {
                        is ThreadPost.ViewablePost -> parent.post.uri == uri
                        is ThreadPost.BlockedPost -> parent.uri == uri
                        is ThreadPost.NotFoundPost -> parent.uri == uri
                    }
                } || thread.replies.any { reply ->
                    when(reply) {
                        is ThreadPost.ViewablePost -> reply.post.uri == uri
                        is ThreadPost.BlockedPost -> reply.uri == uri
                        is ThreadPost.NotFoundPost -> reply.uri == uri
                    }
                }
            }
            is FeedInfo -> feed.uri == uri
            is ListInfo -> list.uri == uri
            is ModLabel -> label.identifier == uri.atUri
            is ProfileItem -> false
        }
    }

    fun getUris(): List<AtUri> {
        return when(this) {
            is Post -> listOf(post.uri)
            is Thread -> {
                ( thread.parents.map { parent ->
                    when(parent) {
                        is ThreadPost.ViewablePost -> parent.post.uri
                        is ThreadPost.BlockedPost -> parent.uri
                        is ThreadPost.NotFoundPost -> parent.uri
                    }
                } + listOf(thread.post.uri) + thread.replies.map { reply ->
                    when(reply) {
                        is ThreadPost.ViewablePost -> reply.post.uri
                        is ThreadPost.BlockedPost -> reply.uri
                        is ThreadPost.NotFoundPost -> reply.uri
                    }
                }).filterNotNull()
            }
            is FeedInfo -> listOf(feed.uri)
            is ListInfo -> listOf(list.uri)
            is ModLabel -> listOf()
            is ProfileItem -> listOf()
        }
    }

    fun getUri(): AtUri? {
        return when(this) {
            is Post -> post.uri
            is Thread -> thread.post.uri
            is FeedInfo -> feed.uri
            is ListInfo -> list.uri
            is ModLabel -> null
            is ProfileItem -> null
        }
    }


}

@Immutable
@Serializable
data class AuthorContext(
    val author: Profile,
    val parentAuthor: Profile? = null,
    val grandParentAuthor: Profile? = null,
    val rootAuthor: Profile? = null,
)

fun PostView.parentOrNull(posts: List<PostView>): ThreadPost? {
    val post = this.toPost()
    val parentUri = post.reply?.replyRef?.parent?.uri
    return if(parentUri != null) (posts.firstOrNull { it.uri == parentUri })?.toPost()?.let { bskyPost ->
        val recParent = parentOrNull(posts.filterNot { it.uri == parentUri })
        ThreadPost.ViewablePost(bskyPost, recParent, listOf(
            ThreadPost.ViewablePost(post, ThreadPost.ViewablePost(bskyPost, recParent, listOf()), listOf())
        ))
    } else null
}

fun PostView.toThreadPost(posts: List<PostView>): ThreadPost {
    val parent = this.parentOrNull(posts.filterNot { it.uri == this.uri })
    return ThreadPost.ViewablePost(this.toPost(), parent, listOf())
}

fun BskyPost.toThreadPost(reply: BskyPost? = null): ThreadPost {
    val parent = this.reply?.parentPost
    return ThreadPost.ViewablePost(this, parent?.toThreadPost(),
        reply?.let { listOf(it.toThreadPost())} ?: listOf())
}

fun FeedViewPost.toThreadPost(reply: BskyPost? = null): ThreadPost {
    val post = this.toPost()
    val parent = post.reply?.parentPost
    return ThreadPost.ViewablePost(post, parent?.toThreadPost(post),
        reply?.let { listOf(it.toThreadPost())} ?: listOf())
}

enum class PostStatus {
    Viewable,
    NotFound,
    Blocked,
}

inline fun ReplyRefParentUnion?.getParentStatus(): Pair<PostStatus, AtUri>? {
    return when(this) {
        is ReplyRefParentUnion.BlockedPost -> PostStatus.Blocked to this.value.uri
        is ReplyRefParentUnion.NotFoundPost -> PostStatus.NotFound to this.value.uri
        is ReplyRefParentUnion.PostView -> PostStatus.Viewable to this.value.uri
        null -> null
    }
}

inline fun ReplyRefParentUnion?.postView(): PostView? {
    return when(this) {
        is ReplyRefParentUnion.BlockedPost -> null
        is ReplyRefParentUnion.NotFoundPost -> null
        is ReplyRefParentUnion.PostView -> this.value
        null -> null
    }
}

inline fun ReplyRefRootUnion?.getRootStatus(): Pair<PostStatus, AtUri>? {
    return when(this) {
        is ReplyRefRootUnion.BlockedPost -> PostStatus.Blocked to this.value.uri
        is ReplyRefRootUnion.NotFoundPost -> PostStatus.NotFound to this.value.uri
        is ReplyRefRootUnion.PostView -> PostStatus.Viewable to this.value.uri
        null -> null
    }
}

inline fun ReplyRefRootUnion?.postView(): PostView? {
    return when(this) {
        is ReplyRefRootUnion.BlockedPost -> null
        is ReplyRefRootUnion.NotFoundPost -> null
        is ReplyRefRootUnion.PostView -> this.value
        null -> null
    }
}