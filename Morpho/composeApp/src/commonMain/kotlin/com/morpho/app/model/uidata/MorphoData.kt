package com.morpho.app.model.uidata

//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import androidx.compose.runtime.Immutable
import androidx.compose.ui.util.*
import app.bsky.feed.FeedViewPost
import com.morpho.app.data.BskyUserPreferences
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uistate.FeedType
import com.morpho.butterfly.*
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration


typealias TunerFunction = (List<MorphoDataItem.FeedItem>, FeedTuner) -> List<MorphoDataItem.FeedItem>

@Parcelize
@Immutable
@Serializable
data class AtCursor(val cursor: String?, val scroll: Int): Parcelable {
    companion object {
        val EMPTY: AtCursor = AtCursor(null, 0)
    }
}


@Immutable
@Serializable
data class MorphoData<T: MorphoDataItem>(
    val title: String = "Home",
    val uri: AtUri = AtUri.HOME_URI,
    val cursor: AtCursor = AtCursor.EMPTY,
    val items: List<T> = listOf(),
    //@TypeParceler<JsonElement, JsonElementParceler>()
    val query: JsonElement = JsonObject(emptyMap()),
) {
    companion object {

        fun <T : MorphoDataItem> EMPTY(): MorphoData<T> {
            return MorphoData(
                title = "Home",
                uri = AtUri.HOME_URI,
                cursor = AtCursor.EMPTY,
                items = listOf(),
                query = JsonObject(emptyMap()),
            )
        }

        fun <T : MorphoDataItem> fromList(
            title: String = "Home",
            uri: AtUri = AtUri.HOME_URI,
            cursor: AtCursor = AtCursor.EMPTY,
            items: List<T>,
            query: JsonElement = JsonObject(emptyMap()),
        ): MorphoData<T> {
            return MorphoData(
                title = title,
                uri = uri,
                cursor = cursor,
                items = items,
                query = query,
            )
        }

        fun <T: MorphoDataItem.FeedItem> fromFeed(
            feedPosts: List<FeedViewPost>,
            cursor: AtCursor = AtCursor.EMPTY,
            title: String = "Home",
            uri: AtUri = AtUri.HOME_URI,
            query: JsonElement = JsonObject(emptyMap()),
        ): MorphoData<MorphoDataItem.FeedItem> {
            val items = feedPosts.map { item ->
                MorphoDataItem.FeedItem.fromFeedViewPost(item)
            }
            return MorphoData(
                title = title,
                uri = uri,
                cursor = cursor,
                items = items,
                query = query,
            )
        }

        fun concatFeed(
            query: JsonElement,
            responseCursor: String?,
            oldCursor: AtCursor,
            feed: List<FeedViewPost>,
            data: MorphoData<MorphoDataItem.FeedItem>,
            uri: AtUri = data.uri,
            title: String = data.title,
            api: Butterfly? = null,
        ): Flow<MorphoData<MorphoDataItem.FeedItem>> = flow {
            val newItems = fromFeed<MorphoDataItem.FeedItem>(
                feed.toList(), AtCursor(responseCursor, 0),
                uri = uri, title = title, query = query).collectThreads().single()
            emit(if (oldCursor != AtCursor.EMPTY && data.items.isNotEmpty()) {
                val newScroll = maxOf(data.items.size, oldCursor.scroll)
                concat(data, newItems, AtCursor(responseCursor, newScroll), query = query)
            } else if (oldCursor == AtCursor.EMPTY && data.items.isNotEmpty()) {
                concat(newItems, data,AtCursor(responseCursor, 0), query = query)
            } else {
                newItems
            })
        }

        fun concatNonThreadedFeed(
            query: JsonElement,
            responseCursor: String?,
            oldCursor: AtCursor,
            feed: List<FeedViewPost>,
            data: MorphoData<MorphoDataItem.FeedItem>,
            uri: AtUri = data.uri,
            title: String = data.title,
        ): MorphoData<MorphoDataItem.FeedItem> {
            val newItems = fromFeed<MorphoDataItem.FeedItem>(
                feed.toList(), AtCursor(responseCursor, 0),
                uri = uri, title = title, query = query)
            return if (oldCursor != AtCursor.EMPTY && data.items.isNotEmpty()) {
                val newScroll = if(oldCursor.scroll == 0) 0 else maxOf(data.items.size, oldCursor.scroll)
                concat(data, newItems, AtCursor(responseCursor, newScroll), query = query)
            } else if (oldCursor == AtCursor.EMPTY && data.items.isNotEmpty()) {
                concat(newItems, data,AtCursor(responseCursor, 0), query = query)
            } else {
                newItems
            }
        }


        fun <T : MorphoDataItem> concat(
            first: MorphoData<T>,
            last: MorphoData<T>,
            cursor: AtCursor = last.cursor,
            query: JsonElement = JsonObject(emptyMap()),
        ): MorphoData<T> {
            return first.copy(
                items = (first.items + last.items).toPersistentList(),
//                    .sortedByDescending {
//                        when (it) {
//                            is MorphoDataItem.Post -> it.post.createdAt
//                            is MorphoDataItem.Thread -> it.thread.post.createdAt
//                            is MorphoDataItem.FeedInfo -> it.feed.indexedAt
//                            is MorphoDataItem.ListInfo -> it.list.indexedAt
//                            is MorphoDataItem.ModLabel -> Moment(Instant.DISTANT_PAST)
//                            is MorphoDataItem.ProfileItem -> Moment(Instant.DISTANT_PAST)
//                            is MorphoDataItem.LabelService -> it.service.indexedAt
//                            else -> {
//                                Moment(Instant.DISTANT_PAST)
//                            }
//                        }
//                    }.toList(),
                cursor = cursor, title = first.title, uri = first.uri
            )
        }

        fun concat(
            first: MorphoData<MorphoDataItem>,
            last: List<MorphoDataItem>,
            cursor: AtCursor = first.cursor,
            query: JsonElement = JsonObject(emptyMap()),
        ): MorphoData<MorphoDataItem> {
            return first.copy(
                items = (first.items + last),
                cursor = cursor, title = first.title, uri = first.uri
            )
        }

        fun <T : MorphoDataItem> concat(
            first: List<T>,
            last: MorphoData<T>,
            cursor: AtCursor = last.cursor,
        ): MorphoData<T> {
            return last.copy(
                items = (first + last.items),
                cursor = cursor, title = last.title, uri = last.uri
            )
        }

        fun concat(
            posts: List<FeedViewPost>,
            feed: MorphoData<MorphoDataItem.FeedItem>,
            cursor: AtCursor = feed.cursor,
            query: JsonElement = JsonObject(emptyMap()),
        ): MorphoData<MorphoDataItem.FeedItem> {
            val new = fromFeed<MorphoDataItem.FeedItem>(
                feedPosts = posts,
                cursor,
                feed.title,
                feed.uri,
                query = feed.query,
            )
            return concat(new, feed, cursor, query)
        }

        fun fromFeedGenList(
            title: String,
            uri: AtUri,
            feeds: List<FeedGenerator>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoData<MorphoDataItem.FeedInfo> {
            return MorphoData(
                title = title,
                uri = uri,
                cursor = cursor,
                items = feeds.map { MorphoDataItem.FeedInfo(it) }.toMutableList(),
            )
        }

        fun fromProfileList(
            title: String,
            uri: AtUri,
            list: List<Profile>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoData<MorphoDataItem.ProfileItem> {
            return MorphoData(
                title = title,
                uri = uri,
                cursor = cursor,
                items = list.map { MorphoDataItem.ProfileItem(it) }.toMutableList(),
            )
        }

        fun fromBskyList(
            title: String,
            uri: AtUri,
            lists: List<BskyList>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoData<MorphoDataItem.ListInfo> {
            return MorphoData(
                title = title,
                uri = uri,
                cursor = cursor,
                items = lists.map { MorphoDataItem.ListInfo(it) }.toMutableList(),
            )
        }

        fun fromModLabelDefs(
            title: String,
            uri: AtUri,
            labels: List<BskyLabelDefinition>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoData<MorphoDataItem.ModLabel> {
            return MorphoData(
                title = title,
                uri = uri,
                cursor = cursor,
                items = labels.map { MorphoDataItem.ModLabel(it) }.toMutableList(),
            )
        }

        fun fromModServiceDefs(
            title: String,
            uri: AtUri,
            services: List<BskyLabelService>,
            cursor: AtCursor = AtCursor.EMPTY,
        ): MorphoData<MorphoDataItem.LabelService> {
            return MorphoData(
                title = title,
                uri = uri,
                cursor = cursor,
                items = services.map { MorphoDataItem.LabelService(it) }.toMutableList(),
            )
        }

    }

    val isHome: Boolean
        get() = uri == AtUri.HOME_URI

    val isProfileFeed: Boolean
        get() = uri.atUri.matches(AtUri.ProfilePostsUriRegex) ||
                uri.atUri.matches(AtUri.ProfileRepliesUriRegex) ||
                uri.atUri.matches(AtUri.ProfileMediaUriRegex) ||
                uri.atUri.matches(AtUri.ProfileLikesUriRegex) ||
                uri.atUri.matches(AtUri.ProfileUserListsUriRegex) ||
                uri.atUri.matches(AtUri.ProfileModServiceUriRegex) ||
                uri.atUri.matches(AtUri.ProfileFeedsListUriRegex)


    val isMyProfile: Boolean
        get() = (isProfileFeed && uri.atUri.contains("me")) || (uri == AtUri.MY_PROFILE_URI)

    val feedType: FeedType
        get() = when {
            isHome -> FeedType.HOME
            uri.atUri.matches(AtUri.ProfilePostsUriRegex) -> FeedType.PROFILE_POSTS
            uri.atUri.matches(AtUri.ProfileRepliesUriRegex) -> FeedType.PROFILE_REPLIES
            uri.atUri.matches(AtUri.ProfileMediaUriRegex) -> FeedType.PROFILE_MEDIA
            uri.atUri.matches(AtUri.ProfileLikesUriRegex) -> FeedType.PROFILE_LIKES
            uri.atUri.matches(AtUri.ProfileUserListsUriRegex) -> FeedType.PROFILE_USER_LISTS
            uri.atUri.matches(AtUri.ProfileModServiceUriRegex) -> FeedType.PROFILE_MOD_SERVICE
            uri.atUri.matches(AtUri.ProfileFeedsListUriRegex) -> FeedType.PROFILE_FEEDS_LIST
            uri.atUri.matches(AtUri.ListFeedUriRegex) -> FeedType.LIST_FOLLOWING
            else -> FeedType.OTHER
        }

    operator fun contains(cid: Cid): Boolean {
        return items.fastAny {
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

    fun collectThreads(
        depth: Int = 3, height: Int = 80,
        timeRange: Delta = Delta(Duration.parse("4h")),
        repliesBumpThreads: Boolean = !isProfileFeed,
        api: Butterfly? = null, // allows to just use local data
    ): Flow<MorphoData<T>> = flow {
        val threads = mutableListOf<MorphoDataItem.Thread?>()
        val replies = mutableListOf<MorphoDataItem.Post?>()
        val posts = mutableListOf<MorphoDataItem.Post?>()
        val threadCandidates = mutableListOf<MorphoDataItem.Thread?>()
        items.fastForEach { item ->
            when(item) {
                is MorphoDataItem.Post -> {
                    if (item.isReply) replies.add(item)
                    else if (item.isOrphan) posts.add(item)
                    else posts.add(item)
                }
                is MorphoDataItem.Thread -> {
                    if (!item.isIncompleteThread) threads.add(item)
                    else threadCandidates.add(item)
                }
                else -> return@fastForEach
            }
        }
        replies.fastForEachIndexed { index, reply ->
            if (reply == null) return@fastForEachIndexed
            if (reply.isOrphan) {
                val parent = reply.post.reply?.parentPost
                    ?: reply.post.reply?.replyRef?.parent?.uri?.let {
                        if (api != null) {
                            getPost(it, api).firstOrNull()
                        } else null
                    }
                val root = reply.post.reply?.rootPost
                    ?: reply.post.reply?.replyRef?.root?.uri?.let {
                        if (api != null) {
                            getPost(it, api).firstOrNull()
                        } else null
                    }
                replies[index] = MorphoDataItem.Post(
                    reply.post.copy(reply = reply.post.reply?.copy(parentPost = parent, rootPost = root)),
                    reply.reason,
                    isOrphan = root != null && parent != null,
                )
            }
            val newReply = replies[index] ?: return@fastForEachIndexed // Update in case we changed it above
            val replyRef = newReply.post.reply?.replyRef ?: return@fastForEachIndexed
            val parent = replyRef.parent.uri
            val root = replyRef.root.uri
            val inThread = threads.indexOfFirst { it?.containsUri(parent) ?: false  || it?.containsUri(root) ?: false }
            if (inThread != -1) {
                val thread = threads.getOrNull(inThread) ?: return@fastForEachIndexed
                threads[inThread] = thread.addReply(newReply.post)
                replies[index] = null
            }
            val inCandidates = threadCandidates.indexOfFirst {  it?.containsUri(parent) ?: false  || it?.containsUri(root) ?: false }
            if (inCandidates != -1) {
                val thread = threadCandidates.getOrNull(inCandidates) ?: return@fastForEachIndexed
                threadCandidates[inCandidates] = thread.addReply(newReply.post)
                replies[index] = null
            }

        }
        threadCandidates.fastForEachIndexed { index, thread ->
            if (thread == null) return@fastForEachIndexed
            val rootInThreads = threads.indexOfFirst { t -> t?.containsUri(thread.rootUri) ?: false }
            if (rootInThreads == - 1) {
                val threadToSplice = threads.getOrNull(rootInThreads) ?: return@fastForEachIndexed
                if(
                    thread.thread.parents.firstOrNull() is ThreadPost.ViewablePost
                    && threadToSplice.thread.parents.firstOrNull() is ThreadPost.ViewablePost
                    && thread.rootUri == threadToSplice.rootUri
                ) {
                    if(thread.thread.parents.size == 1 && threadToSplice.thread.parents.size == 1) {
                        // Both threads have the same, viewable root post and are only one level deep in terms of parents
                        val newEntry = thread.thread.parents.first() as ThreadPost.ViewablePost
                        val oldEntry = threadToSplice.thread.parents.first() as ThreadPost.ViewablePost

                        val newReplies = (newEntry.replies + oldEntry.replies).distinctBy { it.uri }.toMutableList()
                        newReplies.add(ThreadPost.ViewablePost(thread.thread.post, thread.thread.replies))
                        if( thread.getUri() != threadToSplice.getUri() )
                            newReplies.add(ThreadPost.ViewablePost(threadToSplice.thread.post, threadToSplice.thread.replies))
                        val newThread = BskyPostThread(
                            post = newEntry.post,
                            parents = listOf(),
                            replies = newReplies.distinctBy { it.uri },
                        )
                        threads[rootInThreads] = threadToSplice.copy(thread = newThread, isIncompleteThread = false)
                        threadCandidates[index] = null
                    } else if(thread.thread.parents.size == 2 && threadToSplice.thread.parents.size == 2) {
                        // Both threads have the same, viewable root post and parent chains are both length 2
                        val newEntry = thread.thread.parents.first() as ThreadPost.ViewablePost

                        val newReplies = mutableListOf<ThreadPost>()
                        if(thread.thread.parents.lastOrNull() !is ThreadPost.ViewablePost) return@fastForEachIndexed
                        if(threadToSplice.thread.parents.lastOrNull() !is ThreadPost.ViewablePost) return@fastForEachIndexed
                        val newParent = thread.thread.parents.last() as ThreadPost.ViewablePost
                        val oldParent = threadToSplice.thread.parents.last() as ThreadPost.ViewablePost
                        val newReply = ThreadPost.ViewablePost(thread.thread.post, thread.thread.replies)
                        val oldReply = ThreadPost.ViewablePost(threadToSplice.thread.post, threadToSplice.thread.replies)
                        newParent.addReply(newReply)
                        oldParent.addReply(oldReply)
                        newReplies.add(newReply)
                        newReplies.add(oldReply)
                        val newThread = BskyPostThread(
                            post = newEntry.post,
                            parents = listOf(newParent),
                            replies = newReplies.distinctBy { it.uri },
                        )
                        threads[rootInThreads] = threadToSplice.copy(thread = newThread, isIncompleteThread = false)
                        threadCandidates[index] = null
                    }

                }
            } else {
                val inThreads = threads.indexOfFirst { t -> t?.containsUri(thread.thread.post.uri) ?: false }
                if (inThreads == - 1) {
                    val threadToSplice = threads.getOrNull(index) ?: return@fastForEachIndexed
                    threads[index] = threadToSplice.addReply(ThreadPost.ViewablePost(thread.thread.post, thread.thread.replies))
                    threadCandidates[index] = null
                }
            }
        }
        threadCandidates.fastFilterNotNull()
        if (threadCandidates.isNotEmpty()) threads.addAll(threadCandidates)
        val newReplies = replies.filterNotNull()
            .distinctBy { it.getUri() }
            .filterNot { reply ->
                if(reply.isRepost) return@filterNot false
                if(reply.isQuotePost) return@filterNot false
                reply.getUris().any { uri -> threads.any { it?.containsUri(uri) ?: false } }
            }.sortedByDescending { when(it.reason) {
                is BskyPostReason.BskyPostRepost -> it.reason.indexedAt
                else -> it.post.createdAt
            } }.iterator()
        var newPosts = posts.toList().filterNotNull()
        newPosts = newPosts.distinctBy { it.getUri() }
        newPosts = newPosts.filterNot { post ->
                if(post.isRepost) return@filterNot false
                if(post.isQuotePost) return@filterNot false
                post.getUris().any { uri -> threads.any { it?.containsUri(uri) ?: false } }
            }.sortedByDescending { when(it.reason) {
                    is BskyPostReason.BskyPostRepost -> it.reason.indexedAt
                    else -> it.post.createdAt
                } }
        val newPostsIter = newPosts.iterator()
        var newThreads = threads.toList().filterNotNull()
        newThreads = newThreads.sortedByDescending { if(!repliesBumpThreads) {
            it.rootAccessiblePost.createdAt
        } else {
            maxOf(it.thread.post.createdAt,
                  it.thread.replies.fold(it.thread.post.createdAt) { acc, post ->
                      val postTime = when(post) {
                          is ThreadPost.ViewablePost -> post.post.createdAt
                          is ThreadPost.BlockedPost -> Moment(Instant.DISTANT_PAST)
                          is ThreadPost.NotFoundPost -> Moment(Instant.DISTANT_PAST)
                      }
                      maxOf(acc, postTime)
                  })
        } }
        newThreads = newThreads.distinctBy { it.getUri() }
            .filterNot { thread ->
                thread.getUris().filterNot { uri ->
                        newThreads.fastAny { it.getUri() == uri } }.size > 1
            }
        val newThreadsIter = newThreads.iterator()
        val newFeed = mutableListOf<MorphoDataItem.FeedItem>()
        while(newPostsIter.hasNext() || newThreadsIter.hasNext() || newReplies.hasNext() ) {
            if(newPostsIter.hasNext()) newFeed.add(newPostsIter.next())
            if(newThreadsIter.hasNext()) newFeed.add(newThreadsIter.next())
            if(newReplies.hasNext()) newFeed.add(newReplies.next())
        }
        val dedupedFeed = newFeed.distinctBy { it.getUri() }
        //println("New feed:\n${newFeed.joinToString("\n")}")
        val sortedFeed = dedupedFeed.sortedByDescending {
            when(it) {
                is MorphoDataItem.Post -> when(it.reason) {
                    is BskyPostReason.BskyPostFeedPost -> it.post.createdAt
                    is BskyPostReason.BskyPostRepost -> it.reason.indexedAt
                    is BskyPostReason.SourceFeed -> it.post.createdAt
                    null -> it.post.createdAt
                }
                is MorphoDataItem.Thread -> if(!repliesBumpThreads) {
                    it.rootAccessiblePost.createdAt
                } else {
                    maxOf(it.thread.post.createdAt,
                          it.thread.replies.fold(it.thread.post.createdAt) { acc, post ->
                        val postTime = when(post) {
                            is ThreadPost.ViewablePost -> post.post.createdAt
                            is ThreadPost.BlockedPost -> Moment(Instant.DISTANT_PAST)
                            is ThreadPost.NotFoundPost -> Moment(Instant.DISTANT_PAST)
                        }
                        maxOf(acc, postTime)
                    })
                }
            }
        }
        //println("sorted feed:\n${sortedFeed.joinToString("\n")}")
        @Suppress("UNCHECKED_CAST") val newData = copy( items = sortedFeed as List<T>)
        emit(newData)
    }.flowOn(Dispatchers.Default)

    fun dedup(): MorphoData<T> {
        val newList = items.fastDistinctBy { when(it) {
            is MorphoDataItem.FeedItem -> it.key
            is MorphoDataItem.Post -> it.key
            is MorphoDataItem.Thread -> it.key
            is MorphoDataItem.ListInfo -> it.list.uri
            is MorphoDataItem.ModLabel -> it.label.identifier
            is MorphoDataItem.ProfileItem -> it.profile.did
            is MorphoDataItem.LabelService -> it.service.uri
            else -> {it.hashCode()}
        } }
        return this.copy(items = newList)
    }


}

fun MorphoDataFeed<MorphoDataItem>.toMorphoData(
    title: String = "",
    newUri: AtUri? = null
): MorphoData<MorphoDataItem> {
    return MorphoData(
        title = title,
        uri = newUri ?: uri,
        cursor = cursor,
        items = items
    )
}

fun AtUri.id(api:Butterfly): AtIdentifier {
    val idString = atUri.substringAfter("at://").split("/")[0]
    return if (idString == "me") api.atpUser!!.id else {
        // TODO: make this resolve a handle to a DID
        if (idString.contains("did:")) Did(idString) else Handle(idString)
    }
}

fun areSameAuthor(authors: AuthorContext): Boolean {
    val authorDid = authors.author.did
    if(authors.parentAuthor != null && authors.parentAuthor.did != authorDid) {
        return false
    }
    if(authors.grandParentAuthor != null && authors.grandParentAuthor.did != authorDid) {
        return false
    }
    if(authors.rootAuthor != null && authors.rootAuthor.did != authorDid) {
        return false
    }
    return true
}


@Serializable
data class FeedTuner(val tuners: List<TunerFunction> = persistentListOf()) {
    val seenKeys = mutableSetOf<String>()
    val seenUris = mutableSetOf<AtUri>()
    val seenRootUris = mutableSetOf<AtUri>()

    companion object {

        fun useFeedTuners(
            prefs: BskyUserPreferences,
            feed: MorphoData<MorphoDataItem.FeedItem>
        ): List<FeedTuner> {
            if(feed.isProfileFeed) {
                when(feed.feedType) {
                    FeedType.PROFILE_POSTS -> return listOf(FeedTuner(tuners = persistentListOf(::removeReplies)))
                    FeedType.PROFILE_USER_LISTS -> return listOf()
                    FeedType.PROFILE_FEEDS_LIST -> return listOf()
                    FeedType.PROFILE_MOD_SERVICE -> return listOf()
                    else -> {}
                }
            }
            val languages = prefs.preferences.languages.toList()
            val languageTuner: TunerFunction = { f, t ->
                preferredLanguageOnly(languages, f, t)
            }
            if(feed.feedType == FeedType.OTHER) {
                return listOf(FeedTuner(tuners = persistentListOf(languageTuner)))
            }
            if(feed.feedType == FeedType.LIST_FOLLOWING || feed.feedType == FeedType.HOME) {
                val userDid = Did(prefs.user.userDid)
                val tuners = mutableListOf(FeedTuner(tuners = persistentListOf(::removeOrphans)))
                val feedPrefs = prefs.preferences.feedViewPrefs[feed.uri.atUri] ?: return tuners.toList()
                if(feedPrefs.hideReposts) tuners.add(FeedTuner(tuners = persistentListOf(::removeReposts)))
                if(feedPrefs.hideReplies) tuners.add(FeedTuner(tuners = persistentListOf(::removeReplies)))
                else {
                    val followedRepliesOnly: TunerFunction = { f, t ->
                        followedRepliesOnly(userDid, f, t)
                    }
                    tuners.add(FeedTuner(tuners = persistentListOf(followedRepliesOnly)))
                }
                if(feedPrefs.hideQuotePosts) tuners.add(FeedTuner(tuners = persistentListOf(::removeQuotePosts)))
                tuners.add(FeedTuner(tuners = persistentListOf(::dedupThreads)))
                return tuners.toList()
            }
            return listOf()
        }

        fun removeReplies(
            feed: List<MorphoDataItem.FeedItem>,
            tuner: FeedTuner = FeedTuner(),
        ): List<MorphoDataItem.FeedItem> {
            return feed.filterNot { item ->
                when(item) {
                    is MorphoDataItem.Post -> item.isReply && !item.isRepost &&
                            !(item.getAuthors()?.let { areSameAuthor(it) } ?: false)
                    is MorphoDataItem.Thread -> !(item.getAuthors()?.let { areSameAuthor(it) } ?: false)
                }
            }

        }

        fun removeReposts(
            feed: List<MorphoDataItem.FeedItem>,
            tuner: FeedTuner = FeedTuner(),
        ): List<MorphoDataItem.FeedItem> {
            return feed.filterNot { item ->
                item.isRepost
            }
        }

        fun removeQuotePosts(
            feed: List<MorphoDataItem.FeedItem>,
            tuner: FeedTuner = FeedTuner(),
        ): List<MorphoDataItem.FeedItem> {
            return feed.filterNot { item ->
                item.isQuotePost
            }
        }

        fun removeOrphans(
            feed: List<MorphoDataItem.FeedItem>,
            tuner: FeedTuner = FeedTuner(),
        ): List<MorphoDataItem.FeedItem> {
            return feed.filterNot { item ->
                when(item) {
                    is MorphoDataItem.Post -> item.isOrphan
                    is MorphoDataItem.Thread -> false
                }
            }
        }

        fun dedupThreads(
            feed: List<MorphoDataItem.FeedItem>,
            tuner: FeedTuner = FeedTuner(),
        ): List<MorphoDataItem.FeedItem> {
            return feed.filterNot { item ->
                val rootUri = item.rootUri
                if(!item.isRepost == tuner.seenRootUris.contains(rootUri)) {
                    false
                } else {
                    tuner.seenRootUris.add(rootUri)
                    true
                }
            }
        }

        fun followedRepliesOnly(
            userDid: Did,
            feed: List<MorphoDataItem.FeedItem>,
            tuner: FeedTuner = FeedTuner(),
        ): List<MorphoDataItem.FeedItem> {
            return feed.filterNot { item ->
                item.isReply && !shouldDisplayReplyInFollowing(item, userDid)
            }
        }

        fun preferredLanguageOnly(
            languages: List<Language> = persistentListOf(),
            feed: List<MorphoDataItem.FeedItem>,
            tuner: FeedTuner = FeedTuner(),
        ): List<MorphoDataItem.FeedItem> {
            if (languages.isEmpty()) return feed
            val newFeed = feed.filter { item ->
                when(item) {
                    is MorphoDataItem.Post -> {
                        item.post.langs.isEmpty() ||
                        item.post.langs.any { languages.contains(it) }
                    }
                    is MorphoDataItem.Thread -> {
                        item.thread.post.langs.isEmpty() ||
                        item.thread.post.langs.any { languages.contains(it) }
                    }
                }
            }.map { item ->
                when(item) {
                    is MorphoDataItem.Post -> item
                    is MorphoDataItem.Thread -> {
                        item.copy(
                            thread = item.thread.filterReplies { reply ->
                                when(reply) {
                                    is ThreadPost.ViewablePost -> reply.post.langs.isEmpty() ||
                                            reply.post.langs.any { languages.contains(it) }
                                    is ThreadPost.BlockedPost -> true
                                    is ThreadPost.NotFoundPost -> true
                                }

                            }
                        )
                    }
                }
            }
            return newFeed.ifEmpty { feed }
        }
    }
    fun tune(
        feed: MorphoData<MorphoDataItem.FeedItem>
    ): MorphoData<MorphoDataItem.FeedItem> {
        var workingFeed = feed.items
        tuners.forEach { tuner ->
            workingFeed = tuner(workingFeed, this)
        }
        workingFeed = workingFeed.map { item ->
            if(seenKeys.contains(item.key)) return@map null
            else if(item is MorphoDataItem.Thread) {
                val itemUris = item.getUris()
                val seenInThisThread = itemUris.filter { seenUris.contains(it) }
                if(seenInThisThread.isNotEmpty()) {
                   if(seenInThisThread.size == itemUris.size) {
                       return@map null
                   } else {
                       val newParents = item.thread.parents.filter { parent ->
                           when(parent) {
                               is ThreadPost.ViewablePost -> parent.post.uri in seenInThisThread
                               is ThreadPost.BlockedPost -> false
                               is ThreadPost.NotFoundPost -> false
                           }
                       }
                       val newThread = item.copy(thread = item.thread.filterReplies { reply ->
                           when(reply) {
                               is ThreadPost.ViewablePost -> reply.post.uri in seenInThisThread
                               is ThreadPost.BlockedPost -> false
                               is ThreadPost.NotFoundPost -> false
                           }
                       }.copy(parents = newParents))
                       seenUris.addAll(itemUris)
                       if(newThread.thread.replies.isEmpty() && newThread.thread.parents.isEmpty()) {
                           return@map null
                       } else {
                           return@map newThread
                       }
                   }
                } else {
                    seenUris.addAll(itemUris)
                    item
                }
            } else {
                val disableDedub = item.isReply && item.isRepost
                if(!disableDedub) seenKeys.add(item.key)
                item
            }
        }.filterNotNull()
        return feed.copy(items = workingFeed)
    }

}

/// Algo copied from official app
/// https://github.com/bluesky-social/social-app/blob/main/src/lib/api/feed-manip.ts#L445
/// as of commit https://github.com/bluesky-social/social-app/commit/e2a244b99889743a8788b0c464d3e150bc8047ad
/// The algorithm is a controversial, so we may want to change it or offer more options.
fun shouldDisplayReplyInFollowing(
    item: MorphoDataItem.FeedItem,
    userDid: Did,
): Boolean {
    val authors = item.getAuthors()
    val author = authors?.author
    val rootAuthor = authors?.rootAuthor
    val parentAuthor = authors?.parentAuthor
    val grandParentAuthor = authors?.grandParentAuthor
    if (!isSelfOrFollowing(author, userDid))
        return false // Only show replies from self or people you follow.

    if(parentAuthor == null || parentAuthor.did == author?.did
        && rootAuthor == null || rootAuthor?.did == author?.did
        && grandParentAuthor == null || grandParentAuthor?.did == author?.did
    ) return true // Always show self-threads.

    if (
        parentAuthor.did != author?.did &&
        rootAuthor?.did == author?.did &&
        item is MorphoDataItem.Thread
    ) {
        // If you follow A, show A -> someone[>0 likes] -> A chains too.
        // This is different from cases below because you only know one person.
        val parentPost = when(val p = item.thread.parents.lastOrNull()) {
            is ThreadPost.ViewablePost -> p.post
            else -> null
        }
        if(parentPost != null && parentPost.likeCount > 0)
            return true
    }
    // From this point on we need at least one more reason to show it.
    if (
        parentAuthor.did != author?.did && isSelfOrFollowing(parentAuthor, userDid)
    ) return true
    if (
        grandParentAuthor != null &&
        grandParentAuthor.did != author?.did &&
        isSelfOrFollowing(grandParentAuthor, userDid)
    ) return true
    if (
        rootAuthor != null &&
        rootAuthor.did != author?.did &&
        isSelfOrFollowing(rootAuthor, userDid)
    ) return true
    return false
}

fun isSelfOrFollowing(profile: Profile?, userDid: Did): Boolean {
    return profile?.did == userDid || profile?.followedByMe == true
}