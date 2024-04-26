package com.morpho.app.model.bluesky

//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import androidx.compose.ui.util.*
import app.bsky.feed.FeedViewPost
import app.bsky.feed.GetPostThreadQuery
import app.bsky.feed.GetPostThreadResponseThreadUnion
import com.morpho.app.model.uidata.AtCursor
import com.morpho.app.model.uidata.Delta
import com.morpho.app.model.uidata.MorphoData
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlin.time.Duration


typealias TunerFunction = (List<BskyPost>) -> List<BskyPost>




@Suppress("unused")
@Serializable
data class MorphoDataFeed<T: MorphoDataItem> (
    private var _items: MutableList<T> = mutableListOf(),
    var cursor: AtCursor = null,
    val uri: AtUri = AtUri.HOME_URI,
    var hasNewPosts: Boolean = false,
) {
    val items = _items.toImmutableList()
    companion object {

        fun fromPosts(
            posts: List<BskyPost>,
            cursor: AtCursor = null,
        ): MorphoDataFeed<MorphoDataItem.FeedItem> {
            return MorphoDataFeed(
                _items = posts.map { MorphoDataItem.Post(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        fun fromMorphoData(
            data: MorphoData<MorphoDataItem>
        ): MorphoDataFeed<MorphoDataItem> {
            return MorphoDataFeed(
                _items = data.items.toMutableList(),
                cursor = data.cursor, hasNewPosts = data.cursor == null
            )
        }


        fun fromFeedGen(
            feeds: List<FeedGenerator>,
            cursor: AtCursor = null,
        ): MorphoDataFeed<MorphoDataItem.FeedInfo> {
            return MorphoDataFeed(
                _items = feeds.map { MorphoDataItem.FeedInfo(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        fun fromProfileList(
            list: List<Profile>,
            cursor: AtCursor = null,
        ): MorphoDataFeed<MorphoDataItem.ProfileItem> {
            return MorphoDataFeed(
                _items = list.map { MorphoDataItem.ProfileItem(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        fun fromBskyList(
            lists: List<BskyList>,
            cursor: AtCursor = null,
        ): MorphoDataFeed<MorphoDataItem.ListInfo> {
            return MorphoDataFeed(
                _items = lists.map { MorphoDataItem.ListInfo(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        fun fromModLabelDefs(
            labels: List<BskyModLabelDefinition>,
            cursor: AtCursor = null,
        ): MorphoDataFeed<MorphoDataItem.ModLabel> {
            return MorphoDataFeed(
                _items = labels.map { MorphoDataItem.ModLabel(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        fun fromModServiceDefs(
            services: List<BskyLabelService>,
            cursor: AtCursor = null,
        ): MorphoDataFeed<MorphoDataItem.LabelService> {
            return MorphoDataFeed(
                _items = services.map { MorphoDataItem.LabelService(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }


        fun concat(
            posts: List<FeedViewPost>,
            feed: MorphoDataFeed<MorphoDataItem>,
            cursor: AtCursor = feed.cursor,
        ): MorphoDataFeed<MorphoDataItem> {
            return MorphoDataFeed(
                _items = (posts.mapImmutable { MorphoDataItem.Post(it.toPost()) } + feed._items).toList()
                    .toMutableList(),

                cursor = cursor, hasNewPosts = cursor == null
            )
        }
        fun concat(
            feed: MorphoDataFeed<MorphoDataItem>,
            posts: List<FeedViewPost>,
            cursor: AtCursor = feed.cursor,
        ): MorphoDataFeed<MorphoDataItem> {
            return MorphoDataFeed(
                _items = (feed._items + posts.mapImmutable { MorphoDataItem.Post(it.toPost()) })
                    .toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        fun concat(
            first: MorphoDataFeed<MorphoDataItem>,
            last: MorphoDataFeed<MorphoDataItem>,
            cursor: AtCursor = last.cursor
        ): MorphoDataFeed<MorphoDataItem> {
            return MorphoDataFeed(
                _items = (first._items + last._items).toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        fun <T: MorphoDataItem> concat(
            first: MorphoData<T>,
            last: MorphoDataFeed<T>,
            cursor: AtCursor = last.cursor
        ): MorphoDataFeed<T> {
            return MorphoDataFeed(
                _items = (first.items + last.items).toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        fun <T: MorphoDataItem> concat(
            first: MorphoDataFeed<T>,
            last: MorphoData<T>,
            cursor: AtCursor = last.cursor
        ): MorphoDataFeed<T> {
            return MorphoDataFeed(
                _items = (first.items + last.items).toMutableList(),
                cursor = cursor, hasNewPosts = cursor == null
            )
        }

        //@NativeCoroutines
        fun collectThreads(
            list: List<FeedViewPost>,
            depth: Int = 3, height: Int = 10,
            timeRange: Delta = Delta(Duration.parse("4h")),
            cursor: AtCursor = null,
        ): Flow<MorphoDataFeed<MorphoDataItem.FeedItem>> = flow {
            emit(collectThreads(fromPosts(list.toBskyPostList(), cursor), depth, height, timeRange)
                     .distinctUntilChanged().last()
            )
        }.flowOn(Dispatchers.Default)

        //@NativeCoroutines
        fun collectThreads(
            feed: MorphoDataFeed<MorphoDataItem.FeedItem>,
            depth: Int = 3, height: Int = 10,
            timeRange: Delta = Delta(Duration.parse("4h")),
            cursor: AtCursor = feed.cursor,
        ): Flow<MorphoDataFeed<MorphoDataItem.FeedItem>> = flow {
            val threadCandidates = mutableMapOf<Cid, MutableMap<Cid, BskyPost>>()

            feed._items.map { item ->
                if (item is MorphoDataItem.Post) {
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

            val threads = mutableMapOf<Cid, List<BskyPost>>()
            threadCandidates.map { thread ->
                    if (thread.value.values.isNotEmpty()) threads[thread.key] = thread.value.values.toMutableList()
                }
            feed._items.mapIndexed { index, item ->
                if (item is MorphoDataItem.Post){
                    val post = item.post
                    val itemCid = post.cid
                    if( itemCid in threads.keys) {
                        val level = 1
                        val thread = threads[itemCid]
                            ?.filter { (it.createdAt - post.createdAt).duration <= timeRange.duration }
                            ?.sortedByDescending { it.createdAt }
                            .orEmpty()
                        val parents: Flow<ThreadPost> = flow {
                            generateSequence(post.reply?.parent) {
                                it.reply?.parent
                            }.toList().reversed().map { r->
                                emit(ThreadPost.ViewablePost(
                                    r,
                                    findReplies(level, height, r, thread.asFlow())
                                        .toList().toImmutableList()
                                ))
                            }
                        }
                        val replies: Flow<ThreadPost> = flow {
                            threads[itemCid]?.filter {
                                (it.reply?.parent?.cid ?: Cid("")) == itemCid
                            }?.map { p ->
                                emit(ThreadPost.ViewablePost(
                                    p,
                                    findReplies(level, depth, p, thread.asFlow())
                                        .toList().toImmutableList()
                                ))
                            }.orEmpty()
                        }
                        feed._items[index] = MorphoDataItem.Thread(
                            BskyPostThread(
                                post,
                                parents.toList(),
                                replies.toList().toImmutableList()
                            )
                        )
                    }
                }

            }
            feed._items.sortedByDescending {
                when(it) {
                    is MorphoDataItem.Post -> it.post.createdAt
                    is MorphoDataItem.Thread -> it.thread.post.createdAt
                }
            }
            emit(MorphoDataFeed(feed._items, cursor, feed.uri, feed.hasNewPosts))
        }.flowOn(Dispatchers.Default)

        private fun findReplies(level: Int, depth: Int, post: BskyPost, list: Flow<BskyPost>
        ): Flow<ThreadPost> =  flow {
            list.filter {
                (it.reply?.parent?.cid ?: Cid("")) == post.cid
            }.map {
                if (level < depth) {
                    val r = findReplies(level + 1, depth, it, list)
                        .distinctUntilChanged().toList()
                    emit(ThreadPost.ViewablePost(it, r.toImmutableList()))
                } else {
                    emit(ThreadPost.ViewablePost(it))
                }
            }
        }.flowOn(Dispatchers.Default)
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

        fun collectThreads(
            apiProvider: Butterfly,
            cursor: AtCursor = null,
            posts: List<BskyPost>,
            uri: AtUri = AtUri.HOME_URI,
            depth: Long = 1, height: Long = 10,
        ): Flow<MorphoDataFeed<MorphoDataItem>> = flow {
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
            var morphoDataItems: List<MorphoDataItem> = posts.fastMap {
                val thread = threads[it.uri]
                if (threads.containsKey(it.uri) && thread != null) {
                    MorphoDataItem.Thread(thread)
                } else {
                    MorphoDataItem.Post(it)
                }
            }
            morphoDataItems = morphoDataItems.fastFilter { item ->
                threads.none {
                    item is MorphoDataItem.Post && it.value.contains(item.post.uri)
                }
            }
            emit(MorphoDataFeed(_items = morphoDataItems.toMutableList(), cursor, uri, hasNewPosts = cursor == null))
        }.flowOn(Dispatchers.Default)
    }

    fun collectThreads(
        depth: Int = 3, height: Int = 10,
        timeRange: Delta = Delta(Duration.parse("4h"))
    ): Flow<MorphoDataFeed<MorphoDataItem.FeedItem>> = flow {
        emit(collectThreads(this@MorphoDataFeed as MorphoDataFeed<MorphoDataItem.FeedItem>,
                            depth, height, timeRange).distinctUntilChanged().last())
    }.flowOn(Dispatchers.Default)



    operator fun plus(feed: MorphoDataFeed<T>) {
        _items = (_items + feed._items).toMutableList()
        cursor = feed.cursor
    }

    operator fun contains(cid: Cid): Boolean {
        return _items.fastAny {
            when(it) {
                is MorphoDataItem.Post -> it.post.cid == cid
                is MorphoDataItem.Thread -> it.thread.contains(cid)
                is MorphoDataItem.FeedInfo -> it.feed.cid == cid
                is MorphoDataItem.ListInfo -> it.list.cid == cid
                is MorphoDataItem.ModLabel -> false
                is MorphoDataItem.ProfileItem -> false
                is MorphoDataItem.LabelService -> it.service.cid == cid
                else -> {false}
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
    var feed = MorphoDataFeed.dedupPosts(this)
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