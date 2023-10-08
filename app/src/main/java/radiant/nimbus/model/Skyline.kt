package radiant.nimbus.model

import app.bsky.feed.FeedViewPost
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import radiant.nimbus.api.Cid
import radiant.nimbus.util.mapImmutable
import kotlin.time.Duration


data class Skyline(
    var posts: List<SkylineItem>,
    var cursor: String?,
) {
    companion object {
        fun from(
            posts: List<FeedViewPost>,
            cursor: String? = null,
        ): Skyline {
            return Skyline(
                posts = posts.mapImmutable { SkylineItem(it.toPost()) },
                cursor = cursor,
            )
        }

        fun concat(
            posts: List<FeedViewPost>,
            skyline: Skyline,
            cursor: String? = skyline.cursor,
        ): Skyline {
            return Skyline(
                posts = (posts.mapImmutable { SkylineItem(it.toPost()) } union skyline.posts).toList().sortedByDescending { it.post?.createdAt },
                cursor = cursor,
            )
        }
        fun concat(
            skyline: Skyline,
            posts: List<FeedViewPost>,
            cursor: String? = skyline.cursor,
        ): Skyline {
            return Skyline(
                posts = (skyline.posts union posts.mapImmutable { SkylineItem(it.toPost()) }).toList().sortedByDescending { it.post?.createdAt },
                cursor = cursor,
            )
        }

        fun concat(
            first: Skyline,
            last: Skyline,
            cursor: String? = last.cursor
        ): Skyline {
            return Skyline(
                posts = (first.posts union last.posts).toList().sortedByDescending { it.post?.createdAt },
                cursor = cursor,
            )
        }

        suspend fun collectThreads(
            list: List<FeedViewPost>,
            depth: Int = 3, height: Int = 10,
            timeRange: Delta = Delta(Duration.parse("4h")),
            cursor: String? = null,
        ) = CoroutineScope(Dispatchers.Default).async {
            return@async collectThreads(from(list, cursor), depth, height, timeRange).await()
        }

        suspend fun collectThreads(
            skyline: Skyline,
            depth: Int = 3, height: Int = 10,
            timeRange: Delta = Delta(Duration.parse("4h")),
            cursor: String? = skyline.cursor,
        ) = CoroutineScope(Dispatchers.Default).async {
            val threadCandidates = mutableMapOf<Cid, MutableMap<Cid, BskyPost>>()
            async {
                skyline.posts.map { item ->
                    val post = item.post
                    if (post != null) {
                        if(post.reply != null && item.thread == null) {
                            val itemCid = post.cid
                            val parent = post.reply.parent
                            val root = post.reply.root
                            if(itemCid !in threadCandidates.keys) {
                                var found = false
                                threadCandidates.forEach { thread ->
                                    if(itemCid in thread.value.keys) {
                                        if (parent != null && parent.cid !in thread.value.keys) {
                                            thread.value[parent.cid] = parent
                                        }
                                        if (root != null && root.cid !in thread.value.keys) {
                                            thread.value[root.cid] = root
                                        }
                                        item.post = null
                                        found = true
                                        return@forEach
                                    }
                                }
                                if(!found) {
                                    threadCandidates[itemCid] = mutableMapOf()
                                    if (parent != null) threadCandidates[itemCid]?.set(parent.cid, parent )
                                    if (root != null && threadCandidates[itemCid]?.keys?.contains(root.cid) != true ) {
                                        threadCandidates[itemCid]?.set(root.cid, root )
                                    }
                                }
                            } else {
                                if (parent != null && threadCandidates[itemCid]?.keys?.contains(parent.cid) != true ) {
                                    threadCandidates[itemCid]?.set(parent.cid, parent )
                                }
                                if (root != null && threadCandidates[itemCid]?.keys?.contains(root.cid) != true ) {
                                    threadCandidates[itemCid]?.set(root.cid, root )
                                }
                                item.post = null
                            }
                        }
                    }
                }
            }.await()
            val threads = mutableMapOf<Cid, List<BskyPost>>()
            awaitAll(
                async { skyline.posts.filterNot { it.post == null && it.thread == null } },
                async { threadCandidates.map { thread ->
                    if (thread.value.values.isNotEmpty()) threads[thread.key] = thread.value.values.toMutableList()
                } },
            )
            skyline.posts.map { item ->
                val post = item.post
                if (post != null){
                    val itemCid = post.cid
                    if( itemCid in threads.keys) {
                        val level = 1
                        val thread = threads[itemCid]
                            ?.filter { (it.createdAt - post.createdAt).duration <= timeRange.duration }
                            ?.sortedByDescending { it.createdAt }
                            .orEmpty()
                        val parents = async {
                            generateSequence(post.reply?.parent) {
                                it.reply?.parent
                            }.toList().reversed().map { r->
                                ThreadPost.ViewablePost(r, findReplies(level, height, r, thread).await())
                            }
                        }
                        val replies: Deferred<List<ThreadPost>> = async {
                            threads[itemCid]?.filter {
                                (it.reply?.parent?.cid ?: Cid("")) == itemCid
                            }?.map { p ->
                                ThreadPost.ViewablePost(p, findReplies(level, depth, p, thread).await())
                            }.orEmpty()
                        }
                        item.thread = BskyPostThread(post, parents.await(), replies.await().toImmutableList())
                    }
                }

            }
            skyline.posts.sortedByDescending { it.post?.createdAt }
            return@async Skyline(skyline.posts, cursor)
        }

        private fun findReplies(level: Int, depth: Int, post: BskyPost, list: List<BskyPost>
        ) : Deferred<ImmutableList<ThreadPost>> = CoroutineScope(Dispatchers.Default).async {
            list.filter {
                (it.reply?.parent?.cid ?: Cid("")) == post.cid
            }.map {
                if (level < depth) {
                    val r = findReplies(level + 1, depth, it, list)
                    ThreadPost.ViewablePost(it, r.await())
                } else {
                    ThreadPost.ViewablePost(it)
                }
            }.toImmutableList()
        }
    }

    suspend fun collectThreads(
        depth: Int = 3, height: Int = 10,
        timeRange: Delta = Delta(Duration.parse("4h"))
    ) = CoroutineScope(Dispatchers.Default).async {
        return@async Companion.collectThreads(this@Skyline, depth, height, timeRange).await()
    }

    operator fun plus(skyline: Skyline) {
        posts = posts + skyline.posts
        cursor = skyline.cursor
    }

    operator fun contains(cid: Cid): Boolean {
        posts.map { if ((it.post?.cid ?: Cid("")) == cid || (it.thread?.contains(cid) == true)) return true }
        return false
    }

}
