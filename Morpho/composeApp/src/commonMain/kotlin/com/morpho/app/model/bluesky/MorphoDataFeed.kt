package com.morpho.app.model.bluesky

//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import androidx.compose.ui.util.*
import app.bsky.actor.ContentLabelPref
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



typealias TunerFunction = (List<MorphoDataItem.FeedItem>) -> List<MorphoDataItem.FeedItem>




@Suppress("unused", "UNCHECKED_CAST")
@Serializable
data class MorphoDataFeed<T: MorphoDataItem> (
    private var _items: MutableList<T> = mutableListOf(),
    var cursor: AtCursor = AtCursor.EMPTY,
    val uri: AtUri = AtUri.HOME_URI,
    var hasNewPosts: Boolean = false,
) {
    val items: List<T> = _items.toList()
    companion object {

        fun fromPosts(
            posts: List<BskyPost>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoDataFeed<MorphoDataItem.FeedItem> {
            return MorphoDataFeed(
                _items = posts.map { MorphoDataItem.Post(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
            )
        }

        fun fromMorphoData(
            data: MorphoData<MorphoDataItem>
        ): MorphoDataFeed<MorphoDataItem> {
            return MorphoDataFeed(
                _items = data.items.toMutableList(),
                cursor = data.cursor, hasNewPosts = data.cursor == AtCursor.EMPTY
            )
        }




        fun fromFeedGen(
            feeds: List<FeedGenerator>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoDataFeed<MorphoDataItem.FeedInfo> {
            return MorphoDataFeed(
                _items = feeds.map { MorphoDataItem.FeedInfo(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
            )
        }

        fun fromProfileList(
            list: List<Profile>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoDataFeed<MorphoDataItem.ProfileItem> {
            return MorphoDataFeed(
                _items = list.map { MorphoDataItem.ProfileItem(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
            )
        }

        fun fromBskyList(
            lists: List<BskyList>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoDataFeed<MorphoDataItem.ListInfo> {
            return MorphoDataFeed(
                _items = lists.map { MorphoDataItem.ListInfo(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
            )
        }

        fun fromModLabelDefs(
            labels: List<BskyLabelDefinition>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoDataFeed<MorphoDataItem.ModLabel> {
            return MorphoDataFeed(
                _items = labels.map { MorphoDataItem.ModLabel(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
            )
        }

        fun fromModServiceDefs(
            services: List<BskyLabelService>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoDataFeed<MorphoDataItem.LabelService> {
            return MorphoDataFeed(
                _items = services.map { MorphoDataItem.LabelService(it) }.toMutableList(),
                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
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

                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
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
                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
            )
        }

        fun concat(
            first: MorphoDataFeed<MorphoDataItem>,
            last: MorphoDataFeed<MorphoDataItem>,
            cursor: AtCursor = last.cursor
        ): MorphoDataFeed<MorphoDataItem> {
            return MorphoDataFeed(
                _items = (first._items + last._items).toMutableList(),
                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
            )
        }

        fun <T: MorphoDataItem> concat(
            first: MorphoData<T>,
            last: MorphoDataFeed<T>,
            cursor: AtCursor = last.cursor
        ): MorphoDataFeed<T> {
            return MorphoDataFeed(
                _items = (first.items + last.items).toMutableList(),
                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
            )
        }

        fun <T: MorphoDataItem> concat(
            first: MorphoDataFeed<T>,
            last: MorphoData<T>,
            cursor: AtCursor = last.cursor
        ): MorphoDataFeed<T> {
            return MorphoDataFeed(
                _items = (first.items + last.items).toMutableList(),
                cursor = cursor, hasNewPosts = cursor == AtCursor.EMPTY
            )
        }

        //@NativeCoroutines
        fun collectThreads(
            list: List<FeedViewPost>,
            depth: Int = 3, height: Int = 10,
            timeRange: Delta = Delta(Duration.parse("4h")),
            cursor: AtCursor = AtCursor.EMPTY,
        ): Flow<MorphoDataFeed<MorphoDataItem.FeedItem>> = flow {
            emit(collectThreads(fromPosts(list.toBskyPostList().fastMap {
                when(it) {
                    is MorphoDataItem.Post -> it.post
                    is MorphoDataItem.Thread -> it.thread.post
                }
            }, cursor), depth, height, timeRange)
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
                        val parent = post.reply.parentPost
                        val root = post.reply.rootPost
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
                                    if(parent.reply?.parentPost != null) {
                                        thread.value[parent.reply.parentPost.cid] = parent.reply.parentPost
                                    }
                                }
                            }
                            if(!found) {
                                threadCandidates[itemCid] = mutableMapOf()
                                if (parent != null) {
                                    threadCandidates[itemCid]?.set(parent.cid, parent )
                                    if(parent.reply?.parentPost != null) {
                                        threadCandidates[itemCid]?.set(parent.reply.parentPost.cid, parent.reply.parentPost)
                                    }
                                }
                                if (root != null && threadCandidates[itemCid]?.keys?.contains(root.cid) != true ) {
                                    threadCandidates[itemCid]?.set(root.cid, root )
                                }
                            }
                        } else {
                            if (parent != null && threadCandidates[itemCid]?.keys?.contains(parent.cid) != true ) {
                                threadCandidates[itemCid]?.set(parent.cid, parent )
                                if(parent.reply?.parentPost != null) {
                                    threadCandidates[itemCid]?.set(parent.reply.parentPost.cid, parent.reply.parentPost)
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
                            generateSequence(post.reply?.parentPost) {
                                it.reply?.parentPost
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
                                (it.reply?.parentPost?.cid ?: Cid("")) == itemCid
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
                                parents.toList().toImmutableList(),
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
                (it.reply?.parentPost?.cid ?: Cid("")) == post.cid
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
            posts: List<MorphoDataItem.FeedItem>,
            prefs: BskyFeedPref,
            follows: List<Did> = persistentListOf(),
        ): List<MorphoDataItem.FeedItem> {
            var feed = posts.fastFilter { post -> // A-B test perf with fast and normal filter
                if (post is MorphoDataItem.Post) {
                    (!prefs.hideReposts && post.reason is BskyPostReason.BskyPostRepost)
                            || (!prefs.hideQuotePosts && isQuotePost(post.post))
                            || ((!prefs.hideReplies && (post.post.reply != null))
                            && (isSelfReply(post.post) || isThreadRoot(post.post) || post.post.reposted
                            || (post.post.likeCount <= prefs.hideRepliesByLikeCount) )
                            && (!prefs.hideRepliesByUnfollowed && isFollowingAllAuthors(post.post, follows)) )
                            || (post.post.reply == null && !isQuotePost(post.post) && post.reason == null)
                } else if (post is MorphoDataItem.Thread) {
                            (!prefs.hideQuotePosts && isQuotePost(post.thread.post))
                            || ((!prefs.hideReplies && (post.thread.post.reply != null))
                            && (isSelfReply(post.thread.post) || isThreadRoot(post.thread.post) || post.thread.post.reposted
                            || (post.thread.post.likeCount <= prefs.hideRepliesByLikeCount) )
                            && (!prefs.hideRepliesByUnfollowed && isFollowingAllAuthors(post.thread.post, follows)) )
                            || (post.thread.post.reply == null && !isQuotePost(post.thread.post) && post.thread.post.reason == null)
                } else false
            }
            feed = filterByLanguage(feed, prefs.languages)
            feed = filterByContentLabel(feed, prefs.labelsToHide)
            return feed
        }



        fun filterByLanguage(
            posts: List<MorphoDataItem.FeedItem>,
            languages: List<Language>,
        ): List<MorphoDataItem.FeedItem> {
            if (languages.isEmpty()) return posts
            return posts.fastFilter { post ->
                when(post) {
                    is MorphoDataItem.Post -> post.post.langs.any { languages.contains(it) }
                    is MorphoDataItem.Thread -> post.thread.post.langs.any { languages.contains(it) }
                }
            }
        }

        fun filterByContentLabel(
            posts: List<MorphoDataItem.FeedItem>,
            toHide: List<ContentLabelPref> = persistentListOf(),
        ): List<MorphoDataItem.FeedItem> {
            return posts.fastFilter { post ->
                when(post) {
                    is MorphoDataItem.Post -> post.post.labels.none { label -> toHide.fastAny { it.label == label.value } }
                    is MorphoDataItem.Thread -> post.thread.post.labels.none { label -> toHide.fastAny { it.label == label.value } }
                }
            }
        }

        fun filterBy(did: Did, posts: List<MorphoDataItem.FeedItem>): List<MorphoDataItem.FeedItem> {
            return posts.fastFilter {
                when(it) {
                    is MorphoDataItem.Post -> it.post.author.did != did
                    is MorphoDataItem.Thread -> it.thread.post.author.did != did
                }
            }
        }

        fun filterBy(string: String, posts: List<MorphoDataItem.FeedItem>) : List<MorphoDataItem.FeedItem> {
            return posts.fastFilter {
                when(it) {
                    is MorphoDataItem.Post -> it.post.text.contains(string)
                    is MorphoDataItem.Thread -> {
                        it.thread.post.text.contains(string) || it.thread.parents.any { parent ->
                            if (parent is ThreadPost.ViewablePost) {
                                parent.post.text.contains(string)
                            } else false
                        } || it.thread.replies.any { reply ->
                            if (reply is ThreadPost.ViewablePost) {
                                reply.post.text.contains(string)
                            } else false
                        }
                    }
                }
            }
        }

        fun filterByWord(string: String, posts: List<MorphoDataItem.FeedItem>) : List<MorphoDataItem.FeedItem> {
            return filterBy(Regex("""\b$string\b"""), posts)
        }

        fun filterBy(regex: Regex, posts: List<MorphoDataItem.FeedItem>) : List<MorphoDataItem.FeedItem> {
            return posts.fastFilter {
                when(it) {
                    is MorphoDataItem.Post -> it.post.text.contains(regex)
                    is MorphoDataItem.Thread -> {
                        it.thread.post.text.contains(regex) || it.thread.parents.any { parent ->
                            if (parent is ThreadPost.ViewablePost) {
                                parent.post.text.contains(regex)
                            } else false
                        } || it.thread.replies.any { reply ->
                            if (reply is ThreadPost.ViewablePost) {
                                reply.post.text.contains(regex)
                            } else false
                        }
                    }
                }
            }
        }

        fun dedupPosts(posts: List<MorphoDataItem>): List<MorphoDataItem> {
            return posts.fastDistinctBy { post-> // A-B test perf with fast and normal distinctBy
                when(post) {
                    is MorphoDataItem.Post -> post.post.cid
                    is MorphoDataItem.Thread -> post.thread.post.cid
                    is MorphoDataItem.FeedInfo -> post.feed.cid
                    is MorphoDataItem.ListInfo -> post.list.cid
                    is MorphoDataItem.ModLabel -> post.label.identifier
                    is MorphoDataItem.ProfileItem -> post.profile.did
                    is MorphoDataItem.LabelService -> post.service.cid
                }
            }
        }

        fun collectThreads(
            apiProvider: Butterfly,
            cursor: AtCursor = AtCursor.EMPTY,
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
                    if (reply.author.did == post.reply?.rootPost?.author?.did
                        && post.author.did == post.reply.rootPost.author.did
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
            emit(MorphoDataFeed(_items = morphoDataItems.toMutableList(), cursor, uri, hasNewPosts = cursor == AtCursor.EMPTY))
        }.flowOn(Dispatchers.Default)
    }

    fun collectThreads(
        depth: Int = 3, height: Int = 80,
        timeRange: Delta = Delta(Duration.parse("4h"))
    ): Flow<MorphoDataFeed<MorphoDataItem.FeedItem>> = flow {
        emit(collectThreads(this@MorphoDataFeed as MorphoDataFeed<MorphoDataItem.FeedItem>,
                            depth, height, timeRange).distinctUntilChanged().last())
    }.flowOn(Dispatchers.Default)

    fun dedupPosts() {
        _items = Companion.dedupPosts(_items.toList()).toMutableList() as MutableList<T>
    }

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
fun List<FeedViewPost>.toBskyPostList(): List<MorphoDataItem.FeedItem> {
    return this.fastMap { MorphoDataItem.Post(it.toPost()) }
}

@Suppress("UNCHECKED_CAST")
fun List<MorphoDataItem.FeedItem>.tune(
    tuners: List<TunerFunction> = persistentListOf(),
) : List<MorphoDataItem.FeedItem> {
    var feed = MorphoDataFeed.dedupPosts(this )
    tuners.fastForEach { tuner->
        feed = tuner(feed as List<MorphoDataItem.FeedItem>)
    }
    return feed as List<MorphoDataItem.FeedItem>
}


fun isFollowingAllAuthors(post: BskyPost, follows: List<Did>): Boolean {
    return follows.fastAny {
        (post.author.did == it
            || post.reply?.parentPost?.author?.did == it
            || post.reply?.rootPost?.author?.did == it)
    }
}

fun isQuotePost(post: BskyPost) : Boolean {
    return when(post.feature) {
        is BskyPostFeature.MediaRecordFeature -> true
        is BskyPostFeature.RecordFeature -> true
        else ->  false
    }
}

fun isSelfReply(post: BskyPost) : Boolean {
    return if (post.reply != null) {
        if(post.reply.parentPost?.author?.did == post.author.did) {
            true
        } else post.reply.rootPost?.author?.did == post.author.did
    } else {
         false
    }
}

fun getIfSelfReply(post: BskyPost) : BskyPost? {
    return if (post.reply != null) {
        if(post.reply.parentPost?.author?.did == post.author.did) {
            post.reply.parentPost
        } else if (post.reply.rootPost?.author?.did == post.author.did) {
            post.reply.rootPost
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
    return (post.reply?.parentPost == post.reply?.rootPost && post.replyCount > 0)
}