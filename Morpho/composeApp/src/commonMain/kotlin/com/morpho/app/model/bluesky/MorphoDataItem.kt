package com.morpho.app.model.bluesky

import androidx.compose.runtime.Immutable
import app.bsky.feed.*
import com.morpho.app.CommonParcelable
import com.morpho.app.CommonParcelize
import com.morpho.app.CommonRawValue
import com.morpho.app.util.JavaSerializable
import com.morpho.app.util.deserialize
import com.morpho.butterfly.AtUri
import kotlinx.serialization.Serializable


/**
 * Type union for different types of data items that can be displayed in the app.
 * Can use interface directly or use subclasses for more specific types where needed.
 * Would like to figure out how to specify only a subset of types are used in a given context.
 * This would help keep "when" statements from scenario where we want
 *      e.g. PostItems and ThreadItems from needing to handle all possible subtypes.
 */
@Immutable
@Serializable
@CommonParcelize
sealed interface MorphoDataItem: CommonParcelable, JavaSerializable {

    @Immutable
    @Serializable
    @CommonParcelize
    sealed interface FeedItem: MorphoDataItem {
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
                    return Post(newPost, newPost.reason, isOrphan = true)
                }
                if (reason != null) {
                    return Post(post.toPost(reply.toReply(), reason.toReason()), reason.toReason())
                }
                var isRootBlocked = false
                var isRootNotFound = false
                val root = reply.root
                val rootUri = when(root) {
                    is ReplyRefRootUnion.BlockedPost -> {
                        isRootBlocked = true
                        (reply.root as ReplyRefRootUnion.BlockedPost).value.uri
                    }
                    is ReplyRefRootUnion.NotFoundPost -> {
                        isRootNotFound = true
                        (reply.root as ReplyRefRootUnion.NotFoundPost).value.uri
                    }
                    is ReplyRefRootUnion.PostView -> {
                        (reply.root as ReplyRefRootUnion.PostView).value.uri
                    }
                }
                val parent = when(val parent = reply.parent) {
                    is ReplyRefParentUnion.BlockedPost -> {
                        null
                    }
                    is ReplyRefParentUnion.NotFoundPost -> {
                        null
                    }
                    is ReplyRefParentUnion.PostView -> {
                        (parent as ReplyRefParentUnion.PostView).value
                    }
                }
                items.add(feedPost.post)
                val grandparent = if (!isRootBlocked && !isRootNotFound
                    && when(reply.parent) {
                        is ReplyRefParentUnion.BlockedPost -> {
                            false
                        }
                        is ReplyRefParentUnion.NotFoundPost -> {
                            false
                        }
                        is ReplyRefParentUnion.PostView -> {
                            val parentRef = reply.parent as ReplyRefParentUnion.PostView
                            val parentPost = try {
                                app.bsky.feed.Post.serializer().deserialize(parentRef.value.record)
                            } catch (e: Exception) {
                                null
                            }
                            parentPost?.reply?.parent?.uri == rootUri
                        }
                    }) {
                    root
                } else null
                var isGrandParentBlocked = false
                var isGrandParentNotFound = false
                when(grandparent) {
                    is ReplyRefRootUnion.BlockedPost -> isGrandParentBlocked = true
                    is ReplyRefRootUnion.NotFoundPost -> isGrandParentNotFound = true
                    is ReplyRefRootUnion.PostView -> {}
                    null -> isGrandParentNotFound = true
                }
                if(parent != null) items.add(0, parent)
                if (isGrandParentBlocked && isGrandParentNotFound) isOrphan = true
                if (isRootBlocked || isRootNotFound) {
                    return Post(post.toPost(reply.toReply(),null), null, isOrphan = true)
                }
                if (rootUri == parent?.uri) {
                    return if (items.size == 1) {
                         Post(post.toPost(reply.toReply(), null), null, isOrphan = isOrphan)
                    } else {
                        val parents = items.map {
                            ThreadPost.ViewablePost(it.toPost(), listOf())
                        }
                        Thread(
                            BskyPostThread(
                                post = post.toPost(reply.toReply(), null),
                                parents = parents,
                                replies = listOf()
                            ),
                            null,
                            isIncompleteThread = isIncompleteThread,
                        )
                    }
                }
                if(root is ReplyRefRootUnion.PostView) items.add(0, root.value)
                if (grandparent != null && grandparent is ReplyRefRootUnion.PostView) {
                    items.add(0, grandparent.value)
                    isIncompleteThread = true
                }
                return if (items.size == 1) {
                    Post(post.toPost(reply.toReply(), null), null, isOrphan = isOrphan)
                } else {
                    val parents = items.map {
                        ThreadPost.ViewablePost(it.toPost(), listOf())
                    }
                    Thread(
                        BskyPostThread(
                            post = post.toPost(reply.toReply(), null),
                            parents = parents,
                            replies = listOf()
                        ),
                        null,
                        isIncompleteThread = true,
                    )
                }
            }
        }
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
    }

    @Immutable
    @Serializable
    @CommonParcelize
    data class Post(
        val post: @CommonRawValue BskyPost,
        val reason: @CommonRawValue BskyPostReason? = post.reason,
        val isOrphan: Boolean = false,
    ): FeedItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class Thread(
        val thread: @CommonRawValue BskyPostThread,
        val reason: @CommonRawValue BskyPostReason? = null,
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
        val feed: @CommonRawValue FeedGenerator,
    ): MorphoDataItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class ProfileItem(
        val profile: @CommonRawValue Profile,
    ): MorphoDataItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class ListInfo(
        val list: @CommonRawValue BskyList,
    ): MorphoDataItem


    @Immutable
    @Serializable
    @CommonParcelize
    data class ModLabel(
        val label: @CommonRawValue BskyLabelDefinition,
    ): MorphoDataItem

    @Immutable
    @Serializable
    @CommonParcelize
    data class LabelService(
        val service: @CommonRawValue BskyLabelService,
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
            is LabelService -> service.uri == uri
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
            is LabelService -> listOf(service.uri)
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
            is LabelService -> service.uri
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