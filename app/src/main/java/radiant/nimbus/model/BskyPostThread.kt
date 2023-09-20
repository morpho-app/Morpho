@file:Suppress("MemberVisibilityCanBePrivate")

package radiant.nimbus.model

import ThreadViewPostUnion
import android.util.Log
import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponseThreadUnion
import app.bsky.feed.ThreadViewPost
import app.bsky.feed.ThreadViewPostParentUnion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import radiant.nimbus.api.ApiProvider
import sh.christian.ozone.api.response.AtpResponse

@Serializable
public data class BskyPostThread(
    val entry: BskyPost,
    val depth: Int = 6,
    val parentHeight: Int = 80
) {
    var root: ThreadViewPostUnion? = null
    var cursor: ThreadViewPost? = null
    var hops: Int = 0
    suspend fun getThread(apiProvider: ApiProvider) {
        if (depth <= 1000 && parentHeight <= 1000) {
            try {
                val thread = CoroutineScope(Dispatchers.IO).async {
                    val params =
                        GetPostThreadQueryParams(entry.uri, depth.toLong(), parentHeight.toLong())
                    when (val response = apiProvider.api.getPostThread(params)) {
                        is AtpResponse.Failure -> throw Exception(
                            " ${response.statusCode}, ${response.error}, ${response.response}, ${response.headers}"
                        )
                        is AtpResponse.Success -> response.response

                    }
                }.await()
                root = when (val t = thread.thread) {
                    is GetPostThreadResponseThreadUnion.BlockedPost -> ThreadViewPostUnion.BlockedPost(
                        t.value
                    )

                    is GetPostThreadResponseThreadUnion.NotFoundPost -> ThreadViewPostUnion.NotFoundPost(
                        t.value
                    )

                    is GetPostThreadResponseThreadUnion.ThreadViewPost -> {
                        if (t.value.parent == null) {
                            ThreadViewPostUnion.ThreadViewPost(t.value)
                        } else {
                            traverseThreadUp(t.value.parent!!).await()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PostThreadErr", e.message.toString())
            }
        }
    }
    private suspend fun traverseThreadUp(post: ThreadViewPostParentUnion): Deferred<ThreadViewPostUnion> {
        return CoroutineScope(Dispatchers.Default).async {

            when (post) {
                is ThreadViewPostParentUnion.BlockedPost -> ThreadViewPostUnion.BlockedPost(post.value)
                is ThreadViewPostParentUnion.NotFoundPost ->ThreadViewPostUnion.NotFoundPost(post.value)
                is ThreadViewPostParentUnion.ThreadViewPost -> {
                    val postReturn = ThreadViewPost(post.value.post, post.value.parent, post.value.replies)
                    if (postReturn.post.uri == entry.uri) cursor = postReturn
                    when (val parent = post.value.parent) {
                        is ThreadViewPostParentUnion.BlockedPost -> ThreadViewPostUnion.ThreadViewPost(postReturn)
                        is ThreadViewPostParentUnion.NotFoundPost -> ThreadViewPostUnion.ThreadViewPost(postReturn)
                        is ThreadViewPostParentUnion.ThreadViewPost -> {
                            if(hops < parentHeight) {
                                hops++
                                traverseThreadUp(parent).await()
                            } else {
                                ThreadViewPostUnion.ThreadViewPost(postReturn)
                            }
                        }
                        null -> ThreadViewPostUnion.ThreadViewPost(postReturn)
                    }
                }
            }
        }
    }
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
    suspend fun postToThread(apiProvider: ApiProvider, entryPost:BskyPost? = null, depth: Int = 6, parentHeight: Int = 80) {
        CoroutineScope(Dispatchers.Default).launch {
            val startPost = entryPost ?: post
            if (startPost?.reply != null) {
                val tmpThread = BskyPostThread(startPost, depth, parentHeight)
                tmpThread.getThread(apiProvider)
                thread = tmpThread
            }
        }
    }
}
