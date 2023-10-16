package radiant.nimbus.model

import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastDistinctBy
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import app.bsky.feed.FeedViewPost
import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponseThreadUnion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.BskyFeedPref
import radiant.nimbus.api.Cid
import radiant.nimbus.api.Did
import radiant.nimbus.api.Language
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.util.mapImmutable
import kotlin.time.Duration


typealias TunerFunction = (List<BskyPost>) -> List<BskyPost>

data class Skyline(
    var posts: List<SkylineItem>,
    var cursor: String?,
    var feed: String = "home",
) {
    companion object {

        fun from(
            posts: List<BskyPost>,
            cursor: String? = null,
        ): Skyline {
            return Skyline(
                posts = posts.mapImmutable { SkylineItem(it) },
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
            apiProvider: ApiProvider,
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
                        when (val response = apiProvider.api.getPostThread(
                            GetPostThreadQueryParams(
                                reply.uri,
                                depth,
                                height
                            )
                        )) {
                            is AtpResponse.Failure -> {

                            }

                            is AtpResponse.Success -> {
                                when (val thread = response.response.thread) {
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
            }
            var skylineItems: List<SkylineItem> = posts.fastMap {
                if (threads.containsKey(it.uri)) {
                    SkylineItem(it, threads[it.uri])
                } else {
                    SkylineItem(it)
                }
            }
            skylineItems = skylineItems.fastFilter { item ->
                threads.none {
                    item.thread == null && it.value.contains(item.post?.uri)
                }
            }
            return@async Skyline(posts = skylineItems, cursor)
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
fun List<FeedViewPost>.toBskyPostList(): List<BskyPost> {
    return this.map { it.toPost() }
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

fun isFollowingAllAuthors(post:BskyPost, follows: List<Did>): Boolean {
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