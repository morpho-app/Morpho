package radiant.nimbus.model

import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponseThreadUnion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import radiant.nimbus.api.ApiProvider

/**
 * Mildly dirty fake type union for stuff on skylines
 * Also provides a bit of useful functionality for expanding threads in-place
 */
@Serializable
public data class SkylineItem(
    var post: BskyPost? = null,
    var thread: BskyPostThread? = null,
    val reason: BskyPostReason? = null,
) {

    // Can convert a single post into a thread if the post is part of a thread
    suspend fun postToThread(
        apiProvider: ApiProvider,
        entryPost: BskyPost? = null,
        depth: Int = 6,
        parentHeight: Int = 80
    ) = CoroutineScope(Dispatchers.IO).launch {
        val startPost = entryPost ?: post
        val responseThread =
            startPost?.uri?.let {
                GetPostThreadQueryParams(
                    it,
                    depth.toLong(),
                    parentHeight.toLong()
                )
            }
                ?.let {
                    apiProvider.api
                        .getPostThread(it).maybeResponse()?.thread
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