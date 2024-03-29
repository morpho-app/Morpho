package com.morpho.app.model

import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastDistinctBy
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import app.bsky.feed.FeedViewPost
import app.bsky.feed.GetPostThreadQuery
import app.bsky.feed.GetPostThreadResponseThreadUnion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Language
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.Butterfly
import kotlin.time.Duration


typealias TunerFunction = (List<BskyPost>) -> List<BskyPost>

data class Skyline(
    private var _posts: MutableList<SkylineItem> = mutableListOf(),
    var cursor: String?,
    var feed: String = "home",
    var hasNewPosts: Boolean = false,
) {
    val posts = _posts.toImmutableList()
    companion object {

        fun from(
            posts: List<BskyPost>,
            cursor: String? = null,
        ): Skyline {
            return Skyline(
                _posts = posts.map { SkylineItem.PostItem(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        fun concat(
            posts: List<FeedViewPost>,
            skyline: Skyline,
            cursor: String? = skyline.cursor,
        ): Skyline {
            return Skyline(
                _posts = (posts.mapImmutable { SkylineItem.PostItem(it.toPost()) } union skyline._posts).toList()
                    .sortedByDescending {
                        when(it) {
                            is SkylineItem.PostItem -> it.post.createdAt
                            is SkylineItem.ThreadItem -> it.thread.post.createdAt
                        }
                    }.toMutableList(),

                cursor = cursor, hasNewPosts = cursor == null
            )
        }
        fun concat(
            skyline: Skyline,
            posts: List<FeedViewPost>,
            cursor: String? = skyline.cursor,
        ): Skyline {
            return Skyline(
                _posts = (skyline._posts union posts.mapImmutable { SkylineItem.PostItem(it.toPost()) }).toList()
                    .sortedByDescending {
                        when(it) {
                            is SkylineItem.PostItem -> it.post.createdAt
                            is SkylineItem.ThreadItem -> it.thread.post.createdAt
                        }
                    }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        fun concat(
            first: Skyline,
            last: Skyline,
            cursor: String? = last.cursor
        ): Skyline {
            return Skyline(
                _posts = (first._posts union last._posts).toList()
                    .sortedByDescending {
                       when(it) {
                           is SkylineItem.PostItem -> it.post.createdAt
                           is SkylineItem.ThreadItem -> it.thread.post.createdAt
                       }
                }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        suspend fun collectThreads(
            list: List<FeedViewPost>,
            depth: Int = 3, height: Int = 10,
            timeRange: Delta = Delta(Duration.parse("4h")),
            cursor: String? = null,
        ) = CoroutineScope(Dispatchers.Default).async {
            return@async collectThreads(from(list.map{ it.toPost()}, cursor), depth, height, timeRange).await()
        }

        suspend fun collectThreads(
            skyline: Skyline,
            depth: Int = 3, height: Int = 10,
            timeRange: Delta = Delta(Duration.parse("4h")),
            cursor: String? = skyline.cursor,
        ) = CoroutineScope(Dispatchers.Default).async {
            val threadCandidates = mutableMapOf<Cid, MutableMap<Cid, BskyPost>>()
            async {
                skyline._posts.map { item ->
                    if (item is SkylineItem.PostItem) {
                        val post = item.post
                        if(post.reply != null) {
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
                                        found = true
                                        return@forEach
                                    } else if (parent != null && parent.cid in thread.value.keys) {
                                        if(parent.reply?.parent != null) {
                                            thread.value[parent.reply.parent.cid] = parent.reply.parent
                                        }
                                    }
                                }
                                if(!found) {
                                    threadCandidates[itemCid] = mutableMapOf()
                                    if (parent != null) {
                                        threadCandidates[itemCid]?.set(parent.cid, parent )
                                        if(parent.reply?.parent != null) {
                                            threadCandidates[itemCid]?.set(parent.reply.parent.cid, parent.reply.parent)
                                        }
                                    }
                                    if (root != null && threadCandidates[itemCid]?.keys?.contains(root.cid) != true ) {
                                        threadCandidates[itemCid]?.set(root.cid, root )
                                    }
                                }
                            } else {
                                if (parent != null && threadCandidates[itemCid]?.keys?.contains(parent.cid) != true ) {
                                    threadCandidates[itemCid]?.set(parent.cid, parent )
                                    if(parent.reply?.parent != null) {
                                        threadCandidates[itemCid]?.set(parent.reply.parent.cid, parent.reply.parent)
                                    }
                                }
                                if (root != null && threadCandidates[itemCid]?.keys?.contains(root.cid) != true ) {
                                    threadCandidates[itemCid]?.set(root.cid, root )
                                }
                            }
                        }
                    }
                }
            }.await()
            val threads = mutableMapOf<Cid, List<BskyPost>>()
            awaitAll(
                async { threadCandidates.map { thread ->
                    if (thread.value.values.isNotEmpty()) threads[thread.key] = thread.value.values.toMutableList()
                } },
            )
            skyline._posts.mapIndexed { index, item ->
                if (item is SkylineItem.PostItem){
                    val post = item.post
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
                        skyline._posts[index] = SkylineItem.ThreadItem(
                            BskyPostThread(
                                post,
                                parents.await(),
                                replies.await().toImmutableList()
                            )
                        )
                    }
                }

            }
            skyline._posts.sortedByDescending {
                when(it) {
                    is SkylineItem.PostItem -> it.post.createdAt
                    is SkylineItem.ThreadItem -> it.thread.post.createdAt
                }
            }
            return@async Skyline(skyline._posts, cursor)
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
        fun filterByPrefs(
            posts: List<BskyPost>,
            prefs: BskyFeedPref,
            follows: List<Did> = persistentListOf(),
        ): List<BskyPost> {
            var feed = posts.fastFilter { post -> // A-B test perf with fast and normal filter
                (!prefs.hideReposts && post.reason is BskyPostReason.BskyPostRepost)
                        || (!prefs.hideQuotePosts && isQuotePost(post))
                        || ((!prefs.hideReplies && (post.reply != null))
                            && (isSelfReply(post) || isThreadRoot(post) || post.reposted
                                || (post.likeCount <= prefs.hideRepliesByLikeCount) )
                            && (!prefs.hideRepliesByUnfollowed && isFollowingAllAuthors(post, follows)) )
                        || (post.reply == null && !isQuotePost(post) && post.reason == null)
            }
            //feed = filterbyLanguage(feed, prefs.languages)
            feed = filterByContentLabel(feed, prefs.labelsToHide)
            return feed
        }

        fun filterbyLanguage(
            posts: List<BskyPost>,
            languages: List<Language>,
        ): List<BskyPost> {
            return posts.fastFilter { post -> post.langs.any { languages.contains(it) } }
        }

        fun filterByContentLabel(
            posts: List<BskyPost>,
            toHide: List<BskyLabel> = persistentListOf(),
        ): List<BskyPost> {
            return posts.fastFilter { post -> post.labels.none { toHide.contains(it) } }
        }

        fun filterBy(did: Did, posts: List<BskyPost>): List<BskyPost> {
            return posts.fastFilter { it.author.did != did }
        }

        fun filterBy(string: String, posts: List<BskyPost>) : List<BskyPost> {
            return posts.fastFilter {
                it.text.contains(string)
            }
        }

        fun filterByWord(string: String, posts: List<BskyPost>) : List<BskyPost> {
            return filterBy(Regex("""\b$string\b"""), posts)
        }

        fun filterBy(regex: Regex, posts: List<BskyPost>) : List<BskyPost> {
            return posts.fastFilter {
                it.text.contains(regex)
            }
        }

        fun dedupPosts(posts: List<BskyPost>): List<BskyPost> {
            return posts.fastDistinctBy { post-> // A-B test perf with fast and normal distinctBy
                post.cid
            }
        }

        suspend fun collectThreads(
            apiProvider: Butterfly,
            cursor: String? = null,
            posts: List<BskyPost>,
            depth: Long = 1, height: Long = 10,
        ) = CoroutineScope(Dispatchers.IO).async {
            val threads: MutableMap<AtUri, BskyPostThread> = mutableMapOf()
            posts.asReversed().fastMap { post ->
                val reply = getIfSelfReply(post)
                if ((reply != null) && posts.filterNot { it.uri == post.uri }
                        .none { threads.keys.contains(it.uri) || threads.values.any { thread->
                            thread.contains(it.uri)
                        } }) {
                    if (reply.author.did == post.reply?.root?.author?.did
                        && post.author.did == post.reply.root.author.did
                    ) {
                        apiProvider.api.getPostThread(
                            GetPostThreadQuery(
                                reply.uri,
                                depth,
                                height
                            )
                        ).onSuccess {
                                when (val thread = it.thread) {
                                    is GetPostThreadResponseThreadUnion.BlockedPost -> {}
                                    is GetPostThreadResponseThreadUnion.NotFoundPost -> {}
                                    is GetPostThreadResponseThreadUnion.ThreadViewPost -> {
                                        threads[post.uri] = thread.value.toThread()
                                    }
                                }
                            }
                    }
                }
            }
            var skylineItems: List<SkylineItem> = posts.fastMap {
                val thread = threads[it.uri]
                if (threads.containsKey(it.uri) && thread != null) {
                    SkylineItem.ThreadItem(thread)
                } else {
                    SkylineItem.PostItem(it)
                }
            }
            skylineItems = skylineItems.fastFilter { item ->
                threads.none {
                    item is SkylineItem.PostItem && it.value.contains(item.post.uri)
                }
            }
            return@async Skyline(_posts = skylineItems.toMutableList(), cursor, hasNewPosts = cursor == null)
        }



    }

    suspend fun collectThreads(
        depth: Int = 3, height: Int = 10,
        timeRange: Delta = Delta(Duration.parse("4h"))
    ) = CoroutineScope(Dispatchers.Default).async {
        return@async collectThreads(this@Skyline, depth, height, timeRange).await()
    }

    operator fun plus(skyline: Skyline) {
        _posts = (_posts + skyline._posts).toMutableList()
        cursor = skyline.cursor
    }

    operator fun contains(cid: Cid): Boolean {
        return _posts.fastAny {
            when(it) {
                is SkylineItem.PostItem -> it.post.cid == cid
                is SkylineItem.ThreadItem -> it.thread.contains(cid)
            }
        }
    }



}
fun List<FeedViewPost>.toBskyPostList(): List<BskyPost> {
    return this.fastMap { it.toPost() }
}

fun List<BskyPost>.tune(
    tuners: List<TunerFunction> = persistentListOf(),
) : List<BskyPost> {
    var feed = Skyline.dedupPosts(this)
    tuners.fastForEach { tuner->
        feed = tuner(feed)
    }
    return feed
}

fun isFollowingAllAuthors(post: BskyPost, follows: List<Did>): Boolean {
    return follows.fastAny {
        (post.author.did == it
            || post.reply?.parent?.author?.did == it
            || post.reply?.root?.author?.did == it)
    }
}

fun isQuotePost(post: BskyPost) : Boolean {
    return when(post.feature) {
        is BskyPostFeature.MediaPostFeature -> true
        is BskyPostFeature.PostFeature -> true
        else ->  false
    }
}

fun isSelfReply(post: BskyPost) : Boolean {
    return if (post.reply != null) {
        if(post.reply.parent?.author?.did == post.author.did) {
            true
        } else post.reply.root?.author?.did == post.author.did
    } else {
         false
    }
}

fun getIfSelfReply(post: BskyPost) : BskyPost? {
    return if (post.reply != null) {
        if(post.reply.parent?.author?.did == post.author.did) {
            post.reply.parent
        } else if (post.reply.root?.author?.did == post.author.did) {
            post.reply.root
        } else {
            null
        }
    } else {
        null
    }
}

fun isInThread(post: BskyPost) : Boolean {
    return post.reply != null
}

fun isThreadRoot(post: BskyPost) : Boolean {
    return (post.replyCount > 0 && post.reply == null)
}

fun isSecondInThread(post: BskyPost) : Boolean {
    return (post.reply?.parent == post.reply?.root && post.replyCount > 0)
}