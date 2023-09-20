@file:Suppress("MemberVisibilityCanBePrivate")

package radiant.nimbus.model

import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponseThreadUnion
import app.bsky.feed.ThreadViewPost
import app.bsky.feed.ThreadViewPostParentUnion
import app.bsky.feed.ThreadViewPostReplieUnion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.model.ThreadPost.BlockedPost
import radiant.nimbus.model.ThreadPost.NotFoundPost
import radiant.nimbus.model.ThreadPost.ViewablePost

@Serializable
data class BskyPostThread(
    val post: BskyPost,
    val parents: List<ThreadPost>,
    val replies: List<ThreadPost>,
)

@Serializable
sealed interface ThreadPost {
    data class ViewablePost(
        val post: BskyPost,
        val replies: List<ThreadPost>,
    ) : ThreadPost

    object NotFoundPost : ThreadPost

    object BlockedPost : ThreadPost
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
        replies = replies.map { reply -> reply.toThreadPost() },
    )
}

fun ThreadViewPostParentUnion.toThreadPost(): ThreadPost = when (this) {
    is ThreadViewPostParentUnion.ThreadViewPost -> ViewablePost(
        post = value.post.toPost(),
        replies = value.replies.map { it.toThreadPost() }
    )
    is ThreadViewPostParentUnion.NotFoundPost -> NotFoundPost
    is ThreadViewPostParentUnion.BlockedPost -> BlockedPost
}

fun ThreadViewPostReplieUnion.toThreadPost(): ThreadPost = when (this) {
    is ThreadViewPostReplieUnion.ThreadViewPost -> ViewablePost(
        post = value.post.toPost(),
        replies = value.replies.map { it.toThreadPost() },
    )
    is ThreadViewPostReplieUnion.NotFoundPost -> NotFoundPost
    is ThreadViewPostReplieUnion.BlockedPost -> BlockedPost
}


/**
 * Mildly dirty fake type union for stuff on skylines
 * Also provides a bit of useful functionality for expanding threads in-place
 */
@Serializable
public data class SkylineItem(
    var post: BskyPost? = null,
    var thread: BskyPostThread? = null,
) {

    // Can convert a single post into a thread if the post is part of a thread
    suspend fun postToThread(
        apiProvider: ApiProvider,
        entryPost:BskyPost? = null,
        depth: Int = 6,
        parentHeight: Int = 80
    ) = CoroutineScope(Dispatchers.IO).launch {
        val startPost = entryPost ?: post
        val responseThread =
            startPost?.uri?.let { GetPostThreadQueryParams(it, depth.toLong(), parentHeight.toLong()) }
                ?.let {
                    apiProvider.api
                        .getPostThread(it).requireResponse().thread
                }
        //Log.i("responseThread", responseThread.toString())
        when (responseThread) {
            is GetPostThreadResponseThreadUnion.BlockedPost -> {}
            is GetPostThreadResponseThreadUnion.NotFoundPost -> {}
            is GetPostThreadResponseThreadUnion.ThreadViewPost -> thread = responseThread.value.toThread()
            else -> {}
        }
        //Log.i("NewThread", thread.toString())
    }
}
