package com.morpho.app.model.uidata

//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import app.bsky.feed.*
import app.bsky.graph.GetListsQuery
import app.bsky.labeler.GetServicesQuery
import app.bsky.labeler.GetServicesResponseViewUnion
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.bluesky.MorphoDataFeed.Companion.filterByPrefs
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("unused", "UNCHECKED_CAST")
// TODO: Revisit these casts if we can, but they should be safe
class BskyDataService(

): KoinComponent {
    private val api: Butterfly by inject()

    private val dataMap = mutableMapOf<AtUri, MorphoDataFeed>()
    private val _postListFlows = mutableMapOf<AtUri, MutableStateFlow<ImmutableList<MorphoDataItem>>>()
    private val _cursorFlows = mutableMapOf<AtUri, MutableStateFlow<String?>>()

    // Secondary way to make sure you have the most recent stuff, in case you lose the original reference
    val postLists = _postListFlows.mapValues { it.value.asStateFlow() }.toImmutableMap()
    val cursors = _cursorFlows.mapValues { it.value.asStateFlow() }.toImmutableMap()

    //@NativeCoroutines
    suspend fun hasNewPosts(uri: AtUri): Boolean {

        if(dataMap[uri]?.hasNewPosts == true) {
            return true
        } else if(MorphoData.HOME_URI == uri) {
            api.api.getTimeline(GetTimelineQuery(limit = 1, cursor = null))
                .onSuccess { response ->
                    if (response.feed.isNotEmpty()) {
                        val cid = response.feed.first().post.cid
                        if (dataMap[uri]?.contains(cid) == false) {
                            dataMap[uri]?.hasNewPosts = true
                            return true
                        }
                    }
                }.onFailure { }
        } else if(uri.atUri.contains("app.morpho.profile")) {
            val parts = uri.atUri.substringAfter("at://").split("/")
            val id = parts[0]
            val feed = parts[1].substringAfter("profile.")
            val atId = if (id == "me") api.id!! else {
                if (id.contains("did:")) Did(id) else Handle(id)
            }
            when (feed) {
                // Fix this monstrosity maybe?
                "posts" -> {
                    api.api.getAuthorFeed(GetAuthorFeedQuery(atId, 1, null, GetAuthorFeedFilter.POSTS_NO_REPLIES))
                        .onSuccess { response ->
                            if (response.feed.isNotEmpty()) {
                                val cid = response.feed.first().post.cid
                                if (dataMap[uri]?.contains(cid) == false) {
                                    dataMap[uri]?.hasNewPosts = true
                                    return true
                                }
                            }
                        }
                }
                "replies" -> {
                    api.api.getAuthorFeed(GetAuthorFeedQuery(atId, 1, null, GetAuthorFeedFilter.POSTS_WITH_REPLIES))
                        .onSuccess { response ->
                            if (response.feed.isNotEmpty()) {
                                val cid = response.feed.first().post.cid
                                if (dataMap[uri]?.contains(cid) == false) {
                                    dataMap[uri]?.hasNewPosts = true
                                    return true
                                }
                            }
                        }
                }
                "media" -> {
                    api.api.getAuthorFeed(
                        GetAuthorFeedQuery(
                            atId,
                            1,
                            null,
                            GetAuthorFeedFilter.POSTS_WITH_MEDIA
                        )
                    )
                        .onSuccess { response ->
                            if (response.feed.isNotEmpty()) {
                                val cid = response.feed.first().post.cid
                                if (dataMap[uri]?.contains(cid) == false) {
                                    dataMap[uri]?.hasNewPosts = true
                                    return true
                                }
                            }
                        }
                }
                "likes" -> {
                    api.api.getActorLikes(GetActorLikesQuery(atId, 1, null))
                        .onSuccess { response ->
                            if (response.feed.isNotEmpty()) {
                                val cid = response.feed.first().post.cid
                                if (dataMap[uri]?.contains(cid) == false) {
                                    dataMap[uri]?.hasNewPosts = true
                                    return true
                                }
                            }
                        }

                }
                "lists" -> {
                    api.api.getLists(GetListsQuery(atId, 1))
                        .onSuccess { response ->
                            if (response.lists.isNotEmpty()) {
                                val cid = response.lists.first().cid
                                if (dataMap[uri]?.contains(cid) == false) {
                                    dataMap[uri]?.hasNewPosts = true
                                    return true
                                }
                            }
                        }
                }
                "feeds" -> {
                    api.api.getActorFeeds(GetActorFeedsQuery(atId, 1))
                        .onSuccess { response ->
                            if (response.feeds.isNotEmpty()) {
                                val cid = response.feeds.first().cid
                                if (dataMap[uri]?.contains(cid) == false) {
                                    dataMap[uri]?.hasNewPosts = true
                                    return true
                                }
                            }
                        }
                }
                "labelServices" -> {
                    if(atId is Did)
                    api.api.getServices(GetServicesQuery(persistentListOf(atId),true))
                        .onSuccess {
                            if (it.views.isNotEmpty()) {
                                when(it.views.first()) {
                                    is GetServicesResponseViewUnion.LabelerViewDetailed -> {
                                        val cid = (it.views.first() as GetServicesResponseViewUnion.LabelerViewDetailed).value.cid
                                        if (dataMap[uri]?.contains(cid) == false) {
                                            dataMap[uri]?.hasNewPosts = true
                                            return true
                                        }
                                    }

                                    is GetServicesResponseViewUnion.LabelerView -> {
                                        val cid = (it.views.first() as GetServicesResponseViewUnion.LabelerView).value.cid
                                        if (dataMap[uri]?.contains(cid) == false) {
                                            dataMap[uri]?.hasNewPosts = true
                                            return true
                                        }
                                    }

                                }
                            }
                        }
                }
                else -> {}
            }
        } else {
            api.api.getFeed(GetFeedQuery(limit = 1, cursor = null, feed = uri))
                .onSuccess { response ->
                    if (response.feed.isNotEmpty()) {
                        val cid = response.feed.first().post.cid
                        if (dataMap[uri]?.contains(cid) == false) {
                            dataMap[uri]?.hasNewPosts = true
                            return true
                        }
                    }
                }.onFailure {  }
        }
        return false
    }

    //@NativeCoroutines
    suspend fun <T:MorphoDataItem> getFeed(
        uri: AtUri,
        cursor: String? = null,
        limit: Long = 100,
        feedPref: BskyFeedPref? = null,
        follows: List<Profile> = listOf(),
    ): Result<Pair<StateFlow<String?>, StateFlow<ImmutableList<T>>>> {
        return updateFeed<T>(uri, cursor, limit, feedPref, follows).map {
           Pair(_cursorFlows[uri]!!.asStateFlow(), _postListFlows[uri]!!.asStateFlow() as StateFlow<ImmutableList<T>>)
        }
    }

    /**
     * Make sure we get enough posts to not have to reload the feed almost immediately
     * This might happen if the user has aggressive filter settings or is blocked by a lot of people
     */
    //@NativeCoroutines
    suspend fun <T:MorphoDataItem> getFeedWithMinPosts(
        uri: AtUri,
        cursor: String? = null,
        limit: Long = 100,
        feedPref: BskyFeedPref? = null,
        follows: List<Profile> = listOf(),
    ): Result<Pair<StateFlow<String?>, StateFlow<ImmutableList<T>>>> {
        val result = getFeed<T>(uri, cursor, limit, feedPref, follows)
        result.onSuccess {
            if (it.second.first().size < (limit / 5)) {
                // hopefully this just concatenates properly?
                // the internal state should have the previous posts
                return getFeed(uri, it.first.first(), (limit * 2))
            }
        }.onFailure { return Result.failure(it) }
        return Result.failure(Exception("Failed to get feed, unknown error"))
    }

    //@NativeCoroutines
    suspend fun <T:MorphoDataItem> updateFeed(
        uri: AtUri,
        cursor: String? = null,
        limit: Long = 100,
        feedPref: BskyFeedPref? = null,
        follows: List<Profile> = listOf(),
    ): Result<StateFlow<String?>> {
        if(dataMap[uri] == null) dataMap[uri] = MorphoDataFeed()
        var cur = cursor
        if (cursor == "") {
            cur = dataMap[uri]?.cursor
        }
        if(!_cursorFlows.keys.contains(uri)) _cursorFlows[uri] = MutableStateFlow(cur)
        else _cursorFlows[uri]?.value = cur
        when {
            uri == MorphoData.HOME_URI -> {
                api.api.getTimeline(GetTimelineQuery(limit = limit, cursor = cur))
                    .onSuccess { response ->
                        val tuners = mutableListOf<TunerFunction>()
                        if(feedPref != null) tuners.add { posts -> filterByPrefs(posts, feedPref, follows.map { it.did }) }
                        val newPosts = response.feed.toBskyPostList().tune(tuners)
                        if (cur != null) {
                            dataMap[uri] = dataMap[uri]?.let { MorphoDataFeed.concat(it, MorphoDataFeed.collectThreads(api, response.cursor, newPosts).await()) }!!
                        } else {
                            dataMap[uri] = MorphoDataFeed.fromPosts(newPosts, response.cursor)
                        }
                        _cursorFlows[uri]?.update { response.cursor }
                        if (_postListFlows[uri] == null) _postListFlows[uri] = MutableStateFlow(persistentListOf())
                        _postListFlows[uri]?.update { dataMap[uri]!!.items }
                        return Result.success(_cursorFlows[uri]!!.asStateFlow())
                    }.onFailure { return Result.failure(it) }
            }
            uri.atUri.contains("app.morpho.profile") -> {
                updateProfileFeed(uri, cur, limit)
            }
            else -> {
                api.api.getFeed(GetFeedQuery(limit = limit, cursor = cur, feed = uri))
                    .onSuccess { response ->
                        if (response.feed.isNotEmpty()) {
                            val tuners = mutableListOf<TunerFunction>()
                            if (feedPref != null) tuners.add { posts ->
                                filterByPrefs(
                                    posts,
                                    feedPref,
                                    follows.map { it.did })
                            }
                            val newPosts = response.feed.toBskyPostList().tune(tuners)
                            if (cur != null) {
                                dataMap[uri] = dataMap[uri]?.let {
                                    MorphoDataFeed.concat(
                                        it,
                                        MorphoDataFeed.fromPosts(newPosts, response.cursor)
                                    )
                                }!!
                            } else {
                                dataMap[uri] = MorphoDataFeed.fromPosts(newPosts, response.cursor)
                            }
                            _cursorFlows[uri]?.update { response.cursor }
                            if (_postListFlows[uri] == null) _postListFlows[uri] =
                                MutableStateFlow(persistentListOf())
                            _postListFlows[uri]?.update { dataMap[uri]!!.items }
                            return Result.success(_cursorFlows[uri]!!.asStateFlow())
                        }
                    }.onFailure { return Result.failure(it) }
            }
        }
        return Result.failure(Exception("Failed to get feed, unknown error"))
    }

    private suspend fun updateProfileFeed(
        uri: AtUri,
        cursor: String? = null,
        limit: Long = 100,
    ) {
        if (uri.atUri.contains("app.morpho.profile")) {
            val parts = uri.atUri.substringAfter("at://").split("/")
            val id = parts[0]
            val feed = parts[1].substringAfter("profile.")
            val atId = if (id == "me") api.id!! else {
                if (id.contains("did:")) Did(id) else Handle(id)
            }
            when (feed) {
                "posts" -> {
                    api.api.getAuthorFeed(GetAuthorFeedQuery(atId, limit, cursor, GetAuthorFeedFilter.POSTS_NO_REPLIES))
                        .onSuccess { response ->
                            if (cursor != null) {
                                dataMap[uri] = dataMap[uri]?.let {
                                    MorphoDataFeed.concat(
                                        it,
                                        MorphoDataFeed.fromPosts(response.feed.toBskyPostList(), response.cursor)
                                    )
                                }!!
                            } else {
                                dataMap[uri] = MorphoDataFeed.fromPosts(response.feed.toBskyPostList(), response.cursor)
                            }
                            _cursorFlows[uri]?.update { response.cursor }
                            if (_postListFlows[uri] == null) _postListFlows[uri] =
                                MutableStateFlow(persistentListOf())
                            _postListFlows[uri]?.update { dataMap[uri]!!.items }
                        }
                }
                "replies" -> {
                    api.api.getAuthorFeed(GetAuthorFeedQuery(atId, limit, cursor, GetAuthorFeedFilter.POSTS_WITH_REPLIES))
                        .onSuccess { response ->
                            if (cursor != null) {
                                dataMap[uri] = dataMap[uri]?.let {
                                    MorphoDataFeed.concat(
                                        it,
                                        MorphoDataFeed.fromPosts(response.feed.toBskyPostList(), response.cursor)
                                    )
                                }!!
                            } else {
                                dataMap[uri] = MorphoDataFeed.fromPosts(response.feed.toBskyPostList(), response.cursor)
                            }
                            _cursorFlows[uri]?.update { response.cursor }
                            if (_postListFlows[uri] == null) _postListFlows[uri] =
                                MutableStateFlow(persistentListOf())
                            _postListFlows[uri]?.update { dataMap[uri]!!.items }
                        }
                }
                "media" -> {
                    api.api.getAuthorFeed(
                        GetAuthorFeedQuery(
                            atId,
                            limit,
                            cursor,
                            GetAuthorFeedFilter.POSTS_WITH_MEDIA
                        )
                    )
                        .onSuccess { response ->
                            if (cursor != null) {
                                dataMap[uri] = dataMap[uri]?.let {
                                    MorphoDataFeed.concat(
                                        it,
                                        MorphoDataFeed.fromPosts(
                                            response.feed.toBskyPostList(),
                                            response.cursor
                                        )
                                    )
                                }!!
                            } else {
                                dataMap[uri] =
                                    MorphoDataFeed.fromPosts(response.feed.toBskyPostList(), response.cursor)
                            }
                            _cursorFlows[uri]?.update { response.cursor }
                            if (_postListFlows[uri] == null) _postListFlows[uri] =
                                MutableStateFlow(persistentListOf())
                            _postListFlows[uri]?.update { dataMap[uri]!!.items }
                        }
                }
                "likes" -> {
                    api.api.getActorLikes(GetActorLikesQuery(atId, limit, cursor))
                        .onSuccess { response ->
                            if (cursor != null) {
                                dataMap[uri] = dataMap[uri]?.let {
                                    MorphoDataFeed.concat(
                                        it,
                                        MorphoDataFeed.fromPosts(response.feed.toBskyPostList(), response.cursor)
                                    )
                                }!!
                            } else {
                                dataMap[uri] = MorphoDataFeed.fromPosts(response.feed.toBskyPostList(), response.cursor)
                            }
                            _cursorFlows[uri]?.update { response.cursor }
                            if (_postListFlows[uri] == null) _postListFlows[uri] =
                                MutableStateFlow(persistentListOf())
                            _postListFlows[uri]?.update { dataMap[uri]!!.items }
                        }

                }
                "feeds" -> {
                    api.api.getActorFeeds(GetActorFeedsQuery(atId, limit, cursor))
                        .onSuccess { response ->
                            if (cursor != null) {
                                dataMap[uri] = dataMap[uri]?.let {
                                    MorphoDataFeed.concat(
                                        it,
                                        MorphoDataFeed.fromFeedGen(response.feeds.toFeedGenList(), response.cursor)
                                    )
                                }!!
                            } else {
                                dataMap[uri] = MorphoDataFeed.fromFeedGen(response.feeds.toFeedGenList(), response.cursor)
                            }
                            _cursorFlows[uri]?.update { response.cursor }
                            if (_postListFlows[uri] == null) _postListFlows[uri] =
                                MutableStateFlow(persistentListOf())
                            _postListFlows[uri]?.update { dataMap[uri]!!.items }
                        }
                }
                "lists" -> {
                    api.api.getLists(GetListsQuery(atId, limit, cursor))
                        .onSuccess { response ->
                            if (cursor != null) {
                                dataMap[uri] = dataMap[uri]?.let { list ->
                                    MorphoDataFeed.concat(
                                        list,
                                        MorphoDataFeed.fromBskyList(response.lists.map { it.toList() }, response.cursor)
                                    )
                                }!!
                            } else {
                                dataMap[uri] = MorphoDataFeed.fromBskyList(response.lists.map { it.toList() }, response.cursor)
                            }
                            _cursorFlows[uri]?.update { response.cursor }
                            if (_postListFlows[uri] == null) _postListFlows[uri] =
                                MutableStateFlow(persistentListOf())
                            _postListFlows[uri]?.update { dataMap[uri]!!.items }
                        }
                }
                "labelServices" -> {
                    if(atId is Did)
                    api.api.getServices(GetServicesQuery(persistentListOf(atId),true))
                        .onSuccess {
                            val service = (it.views.first() as GetServicesResponseViewUnion.LabelerViewDetailed).value
                            if (cursor != null) {
                                dataMap[uri] = dataMap[uri]?.let { list ->
                                    MorphoDataFeed.concat(
                                        list,
                                        MorphoDataFeed.fromModLabelDefs(service.toLabelService().policies)
                                    )
                                }!!
                            } else {
                                dataMap[uri] = MorphoDataFeed.fromModLabelDefs(service.toLabelService().policies)
                            }
                            //_cursorFlows[uri]?.update { it.cursor }
                            if (_postListFlows[uri] == null) _postListFlows[uri] =
                                MutableStateFlow(persistentListOf())
                            _postListFlows[uri]?.update { dataMap[uri]!!.items }
                        }
                }
                else -> {}
            }
        }
    }

    //@NativeCoroutines
    suspend fun <T:MorphoDataItem> updateFeedWithMinPosts(
        uri: AtUri,
        cursor: String? = null,
        limit: Long = 100,
        feedPref: BskyFeedPref? = null,
        follows: List<Profile> = listOf(),
    ): Result<StateFlow<String?>> {
        return updateFeed<T>(uri, cursor, limit, feedPref, follows).onSuccess {
            return if ((dataMap[uri]?.items?.size ?: 0) < (limit / 5)) {
                // hopefully this just concatenates properly?
                // the internal state should have the previous posts
                updateFeed<T>(uri, it.value, (limit * 2))
            } else Result.success(it)
        }
    }

    fun removeFeed(uri: AtUri): MorphoDataFeed?  {
        _postListFlows.remove(uri)
        _cursorFlows.remove(uri)
        return dataMap.remove(uri)
    }
}