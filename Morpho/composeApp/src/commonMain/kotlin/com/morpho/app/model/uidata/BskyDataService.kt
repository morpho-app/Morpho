package com.morpho.app.model.uidata

//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import app.bsky.actor.GetProfilesQuery
import app.bsky.feed.*
import app.bsky.graph.GetFollowersQuery
import app.bsky.graph.GetFollowsQuery
import app.bsky.graph.GetListsQuery
import app.bsky.labeler.GetServicesQuery
import app.bsky.labeler.GetServicesResponseViewUnion
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.bluesky.MorphoDataFeed.Companion.filterByPrefs
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.ContentLoadingState
import com.morpho.app.model.uistate.FeedType
import com.morpho.app.util.json
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.Did
import kotlinx.collections.immutable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

fun initAtCursor(): MutableSharedFlow<AtCursor> {
    return MutableSharedFlow<AtCursor>(1, 1, BufferOverflow.DROP_OLDEST)
}

suspend fun <T: MorphoDataItem.FeedItem> Flow<Result<MorphoData<T>>>.handleToState(
    default: MorphoData<T>,
    scope: CoroutineScope = BskyDataService.serviceScope,
): StateFlow<ContentCardState.Skyline<T>> = transform {
    if (it.isFailure) {
        emit(ContentCardState.Skyline(default, ContentLoadingState.Error(it.exceptionOrNull()?.message ?: "Failed to load feed"), false))
    } else {
        emit(ContentCardState.Skyline(it.getOrNull() ?: default, ContentLoadingState.Idle, false))
    }
}.stateIn(
    scope,
    SharingStarted.Eagerly,
    ContentCardState.Skyline(default)
)

suspend fun <T: MorphoDataItem> Flow<Result<MorphoData<T>>>.handleToState(
    profile: Profile,
    default: MorphoData<T>,
    scope: CoroutineScope = BskyDataService.serviceScope,
): StateFlow<ContentCardState.ProfileTimeline<T>> = transform {
    if (it.isFailure) {
        emit(ContentCardState.ProfileTimeline(profile, default, ContentLoadingState.Error(it.exceptionOrNull()?.message ?: "Failed to load feed"), false))
    } else {
        emit(ContentCardState.ProfileTimeline(profile, it.getOrNull() ?: default, ContentLoadingState.Idle, false))
    }
}.stateIn(
    scope,
    SharingStarted.Eagerly,
    ContentCardState.ProfileTimeline(profile, default)
)

@Suppress("unused",  "MemberVisibilityCanBePrivate")
// TODO: Revisit these casts if we can, but they should be safe
class BskyDataService(

): KoinComponent {
    private val api: Butterfly by inject()

    private val _dataFlows = mutableMapOf<AtUri, MutableStateFlow<MorphoData<MorphoDataItem>>>()

    private val mutex = Mutex()
    private var timelineTuners = persistentListOf<TunerFunction>()


    // Secondary way to make sure you have the most recent stuff, in case you lose the original reference
    val dataFlows: ImmutableMap<AtUri, StateFlow<MorphoData<MorphoDataItem>>>
            get() = _dataFlows.mapValues { it.value.asStateFlow() }.toImmutableMap()

    companion object {
        val log = logging()
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }



    suspend fun refresh(
        uri: AtUri,
        cursor: AtCursor = null,
    ): Result<StateFlow<MorphoData<MorphoDataItem>>> {
        val flow = dataFlows[uri] ?: return Result.failure(Exception("No feed to refresh."))
        val data = flow.value
        when(data.feedType) {
            FeedType.HOME -> {
                try {
                    val query = Json.decodeFromJsonElement<GetTimelineQuery>(data.query).copy(cursor = cursor)
                    api.api.getTimeline(query).onSuccess { response ->

                        val newPosts = MorphoDataFeed
                            .collectThreads(api, response.cursor, response.feed.toBskyPostList().tune(timelineTuners)).last()
                        val feed = if (cursor != null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(data, newPosts)
                        } else if (cursor == null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(newPosts, data)
                        } else {
                            newPosts
                        }
                        val newData = feed.toMorphoData("Home")
                            .copy(query = json.encodeToJsonElement(query))

                        mutex.withLock {
                            _dataFlows[uri]?.update { newData }
                        }
                        return Result.success(flow)
                    }
                } catch (e: Exception) {
                    log.e { "Failed to refresh feed at $uri.\nError: $e" }
                    return Result.failure(e)
                }
            }
            FeedType.PROFILE_POSTS -> {
                try {
                    val query = Json.decodeFromJsonElement<GetAuthorFeedQuery>(data.query).copy(cursor = cursor)
                    api.api.getAuthorFeed(query).onSuccess { response ->
                        val newPosts = MorphoDataFeed
                            .collectThreads(api, response.cursor, response.feed.toBskyPostList()).last()
                        val feed = if (cursor != null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(data, newPosts)
                        } else if (cursor == null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(newPosts, data)
                        } else {
                            newPosts
                        }
                        val newData = feed.toMorphoData("Posts")
                            .copy(query = json.encodeToJsonElement(query))
                        mutex.withLock {
                            _dataFlows[uri]?.update { newData }
                        }
                        return Result.success(flow)
                    }
                } catch (e: Exception) {
                    log.e { "Failed to refresh feed at $uri.\nError: $e" }
                    return Result.failure(e)
                }
            }
            FeedType.PROFILE_REPLIES -> {
                try {
                    val query = Json.decodeFromJsonElement<GetAuthorFeedQuery>(data.query).copy(cursor = cursor)
                    api.api.getAuthorFeed(query).onSuccess { response ->
                        val newPosts = MorphoDataFeed
                            .collectThreads(api, response.cursor, response.feed.toBskyPostList()).last()
                        val feed = if (cursor != null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(data, newPosts)
                        } else if (cursor == null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(newPosts, data)
                        } else {
                            newPosts
                        }
                        val newData = feed.toMorphoData("Replies")
                            .copy(query = json.encodeToJsonElement(query))
                        mutex.withLock {
                            _dataFlows[uri]?.update { newData }
                        }
                        return Result.success(flow)
                    }
                } catch (e: Exception) {
                    log.e { "Failed to refresh feed at $uri.\nError: $e" }
                    return Result.failure(e)
                }
            }
            FeedType.PROFILE_MEDIA -> {
                try {
                    val query = Json.decodeFromJsonElement<GetAuthorFeedQuery>(data.query).copy(cursor = cursor)
                    api.api.getAuthorFeed(query).onSuccess { response ->
                        val newPosts = MorphoDataFeed
                            .collectThreads(api, response.cursor, response.feed.toBskyPostList()).last()
                        val feed = if (cursor != null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(data, newPosts)
                        } else if (cursor == null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(newPosts, data)
                        } else {
                            newPosts
                        }
                        val newData = feed.toMorphoData("Media")
                            .copy(query = json.encodeToJsonElement(query))
                        mutex.withLock {
                            _dataFlows[uri]?.update { newData }
                        }
                        return Result.success(flow)
                    }
                } catch (e: Exception) {
                    log.e { "Failed to refresh feed at $uri.\nError: $e" }
                    return Result.failure(e)
                }
            }
            FeedType.PROFILE_LIKES -> {
                try {
                    val query = Json.decodeFromJsonElement<GetActorLikesQuery>(data.query).copy(cursor = cursor)
                    api.api.getActorLikes(query).onSuccess { response ->
                        val newPosts = MorphoDataFeed
                            .collectThreads(api, response.cursor, response.feed.toBskyPostList()).last()
                        val feed = if (cursor != null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(data, newPosts)
                        } else if (cursor == null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(newPosts, data)
                        } else {
                            newPosts
                        }
                        val newData = feed.toMorphoData("Likes")
                            .copy(query = json.encodeToJsonElement(query))
                        mutex.withLock {
                            _dataFlows[uri]?.update { newData }
                        }
                        return Result.success(flow)
                    }
                } catch (e: Exception) {
                    log.e { "Failed to refresh feed at $uri.\nError: $e" }
                    return Result.failure(e)
                }
            }
            FeedType.PROFILE_USER_LISTS -> {
                try {
                    val query = Json.decodeFromJsonElement<GetListsQuery>(data.query).copy(cursor = cursor)
                    api.api.getLists(query).onSuccess { response ->
                        val newData = if (cursor != null && data.items.isNotEmpty()) {
                            MorphoData.concat(data, response.lists.mapImmutable { MorphoDataItem.ListInfo(it.toList()) })
                        } else if (cursor == null && data.items.isNotEmpty()) {
                            MorphoData.concat(response.lists.mapImmutable { MorphoDataItem.ListInfo(it.toList()) }, data)
                        } else {
                            MorphoData("Lists", uri, response.cursor,
                                       response.lists.mapImmutable { MorphoDataItem.ListInfo(it.toList()) })
                        }.copy(query = json.encodeToJsonElement(query))
                        mutex.withLock {
                            _dataFlows[uri]?.update { newData as MorphoData<MorphoDataItem>}
                        }
                        return Result.success(flow)
                    }
                } catch (e: Exception) {
                    log.e { "Failed to refresh feed at $uri.\nError: $e" }
                    return Result.failure(e)
                }
            }
            FeedType.PROFILE_MOD_SERVICE -> {
                try {
                    val query = Json.decodeFromJsonElement<GetServicesQuery>(data.query)
                    api.api.getServices(query).onSuccess { response ->
                        val newData = if (cursor != null && data.items.isNotEmpty()) {
                            MorphoData.concat(data, response.views.mapImmutable {
                                when(it) {
                                    is GetServicesResponseViewUnion.LabelerViewDetailed ->
                                        MorphoDataItem.LabelService(it.value.toLabelService())
                                    is GetServicesResponseViewUnion.LabelerView ->
                                        MorphoDataItem.LabelService(it.value.toLabelService())
                                }
                            })
                        } else if (cursor == null && data.items.isNotEmpty()) {
                            MorphoData.concat(response.views.mapImmutable {
                                when(it) {
                                    is GetServicesResponseViewUnion.LabelerViewDetailed ->
                                        MorphoDataItem.LabelService(it.value.toLabelService())
                                    is GetServicesResponseViewUnion.LabelerView ->
                                        MorphoDataItem.LabelService(it.value.toLabelService())
                                }
                            }, data)
                        } else {
                            MorphoData("Services", uri, null,
                                       response.views.mapImmutable {
                                           when(it) {
                                               is GetServicesResponseViewUnion.LabelerViewDetailed ->
                                                   MorphoDataItem.LabelService(it.value.toLabelService())
                                               is GetServicesResponseViewUnion.LabelerView ->
                                                   MorphoDataItem.LabelService(it.value.toLabelService())
                                           }
                                       })
                        }.copy(query = json.encodeToJsonElement(query))
                        mutex.withLock {
                            _dataFlows[uri]?.update { newData as MorphoData<MorphoDataItem>}
                        }
                        return Result.success(flow)
                    }
                } catch (e: Exception) {
                    log.e { "Failed to refresh feed at $uri.\nError: $e" }
                    return Result.failure(e)
                }
            }
            FeedType.PROFILE_FEEDS_LIST -> {
                try {
                    val query = Json.decodeFromJsonElement<GetActorFeedsQuery>(data.query).copy(cursor = cursor)
                    api.api.getActorFeeds(query).onSuccess { response ->
                        val newData = if (cursor != null && data.items.isNotEmpty()) {
                            MorphoData.concat(data, response.feeds.mapImmutable { MorphoDataItem.FeedInfo(it.toFeedGenerator()) })
                        } else if (cursor == null && data.items.isNotEmpty()) {
                            MorphoData.concat(response.feeds.mapImmutable { MorphoDataItem.FeedInfo(it.toFeedGenerator()) }, data)
                        } else {
                            MorphoData("Feeds", uri, response.cursor,
                                       response.feeds.mapImmutable { MorphoDataItem.FeedInfo(it.toFeedGenerator()) })
                        }.copy(query = json.encodeToJsonElement(query))
                        mutex.withLock {
                            _dataFlows[uri]?.update { newData as MorphoData<MorphoDataItem>}
                        }
                        return Result.success(flow)
                    }
                } catch (e: Exception) {
                    log.e { "Failed to refresh feed at $uri.\nError: $e" }
                    return Result.failure(e)
                }
            }
            FeedType.OTHER -> {
                try {
                    val query = Json.decodeFromJsonElement<GetFeedQuery>(data.query).copy(cursor = cursor)
                    api.api.getFeed(query).onSuccess { response ->
                        val tuners = persistentListOf<TunerFunction>()

                        val newPosts = MorphoDataFeed
                            .collectThreads(api, response.cursor, response.feed.toBskyPostList().tune(tuners)).last()
                        val feed = if (cursor != null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(data, newPosts)
                        } else if (cursor == null && data.items.isNotEmpty()) {
                            MorphoDataFeed.concat(newPosts, data)
                        } else {
                            newPosts
                        }
                        val newData = feed.toMorphoData(data.title)
                            .copy(query = json.encodeToJsonElement(query))
                        mutex.withLock {
                            _dataFlows[uri]?.update { newData }
                        }
                        return Result.success(flow)
                    }
                } catch (e: Exception) {
                    log.e { "Failed to refresh feed at $uri.\nError: $e" }
                    return Result.failure(e)
                }
            }
        }
        return Result.failure(Exception("Invalid feed type."))
    }
    @OptIn(FlowPreview::class)
    suspend fun timeline(
        cursor: SharedFlow<AtCursor>,
        limit: Long = 50,
        feedPref: StateFlow<BskyFeedPref> = MutableStateFlow(BskyFeedPref()),
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem.FeedItem>>> = flow {
        cursor.debounce(300).combine(feedPref) { c, f -> c to f }
            .collect { flows ->
                //log.d { "Timeline flow tick." }
                val (cur, pref) = flows
                val prev = dataFlows[AtUri.HOME_URI]?.value
                val query = GetTimelineQuery(limit = limit, cursor = cur)
                api.api.getTimeline(query).onSuccess { response ->
                    val tuners = persistentListOf<TunerFunction>()
                    tuners.add { posts -> filterByPrefs(posts, pref) }

                    val newPosts = MorphoDataFeed
                        .collectThreads(api, response.cursor, response.feed.toBskyPostList().tune(tuners)).last()
                    val feed = if (cur != null && prev != null && prev.items.isNotEmpty()) {
                        MorphoDataFeed.concat(prev, newPosts)
                    } else if (cur == null && prev != null && prev.items.isNotEmpty()) {
                        MorphoDataFeed.concat(newPosts, prev)
                    } else {
                        newPosts
                    }
                    val data = feed.toMorphoData("Home")
                        .copy(query = json.encodeToJsonElement(query))

                    emit(Result.success(data as MorphoData<MorphoDataItem.FeedItem>))
                    log.d{
                        "Timeline " +
                        "Old cursor: $cur " +
                        "New cursor: ${response.cursor}"
                    }
                    log.v {
                        "${data.items.map {
                            when(it) {
                                is MorphoDataItem.Post -> "${it.post.uri}\n"
                                is MorphoDataItem.Thread -> "${it.thread.post.uri}\n"
                            }
                        }}"
                    }
                    mutex.withLock {
                        if(prev == null) _dataFlows[AtUri.HOME_URI] = MutableStateFlow(data)
                        else _dataFlows[AtUri.HOME_URI]?.update { data }
                    }
                }.onFailure {
                    emit(Result.failure(it))
                    log.e { "Failed to get timeline.\nError: $it" }
                    log.v { "Cursor: $cur | Limit: $limit\nFeedPref: $pref\n" }
                }
            }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("Timeline"))
        //.stateIn(scope, SharingStarted.WhileSubscribed(100), Result.success(
        //    MorphoData("Home", AtUri.HOME_URI, null)
        //))

    @OptIn(FlowPreview::class)
    suspend fun feed(
        feedInfo: FeedInfo,
        cursor: SharedFlow<AtCursor>,
        limit: Long = 50,
        feedPref: StateFlow<BskyFeedPref?> = MutableStateFlow(null),
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem.FeedItem>>> = flow {
        cursor.debounce(300).combine(feedPref) { c, f -> c to f }
            .collect { flows ->
                //log.d { "Feed flow tick."}
                val cur = flows.first
                val pref = flows.second
                val prev = dataFlows[feedInfo.uri]?.value
                val query = GetFeedQuery(feedInfo.uri, limit, cur)
                api.api.getFeed(query).onSuccess { response ->
                    val tuners = persistentListOf<TunerFunction>()
                    if (pref != null) tuners.add { posts -> filterByPrefs(posts, pref) }

                    val newPosts = MorphoDataFeed
                        .collectThreads(
                            api,
                            response.cursor,
                            response.feed.toBskyPostList().tune(tuners),
                            feedInfo.uri
                        ).last()
                    val feed = if (cur != null && prev != null && prev.items.isNotEmpty()) {
                        MorphoDataFeed.concat(prev, newPosts)
                    } else if (cur == null && prev != null && prev.items.isNotEmpty()) {
                        MorphoDataFeed.concat(newPosts, prev)
                    } else {
                        newPosts
                    }
                    val data = feed.toMorphoData(feedInfo.name)
                        .copy(query = json.encodeToJsonElement(query))

                    emit(Result.success(data as MorphoData<MorphoDataItem.FeedItem>))
                    log.d{
                        "Feed: ${feedInfo.name} " +
                        "Old cursor: $cur " +
                        "New cursor: ${response.cursor}"
                    }
                    log.v {
                        "${data.items.map {
                            when(it) {
                                is MorphoDataItem.Post -> it.post.uri
                                is MorphoDataItem.Thread -> it.thread.post.uri
                            }
                        }}"
                    }
                    mutex.withLock {
                        if(prev == null) _dataFlows[feedInfo.uri] = MutableStateFlow(data)
                        else _dataFlows[feedInfo.uri]?.update { data }
                    }
                }.onFailure {
                    emit(Result.failure(it))
                    log.e { "Failed to get feed at ${feedInfo.uri}.\nError: $it" }
                    log.v { "Cursor: $cur | Limit: $limit\nFeedPref: $pref" }
                }
            }
    }.distinctUntilChanged().flowOn(dispatcher)

    suspend fun following(
        id: AtIdentifier,
        cursor: SharedFlow<AtCursor>,
        limit: Long = 50,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem.ProfileItem>>> = flow {
        val uri = AtUri.followsUri(id)
        cursor.collect { cur ->
            val prev = dataFlows[uri]?.value
            val query = GetFollowsQuery(id, limit, cur)
            api.api.getFollows(query).onSuccess { response ->
                val data = if (cur != null && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(prev, response.follows.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) })
                } else if (cur == null && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(response.follows.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) }, prev)
                } else {
                    MorphoData("Following", uri, response.cursor,
                               response.follows.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) })
                }.copy(query = json.encodeToJsonElement(query))
                emit(Result.success(data as MorphoData<MorphoDataItem.ProfileItem>))
                mutex.withLock {
                    if(prev == null) _dataFlows[uri] = MutableStateFlow(data as MorphoData<MorphoDataItem>)
                    else _dataFlows[uri]?.update { data as MorphoData<MorphoDataItem> }
                }
            }.onFailure {
                emit(Result.failure(it))
                log.e { "Failed to get follows for $id.\nError: $it" }
                log.v { "Cursor: $cur | Limit: $limit" }
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("Follows of $id"))

    suspend fun followers(
        id: AtIdentifier,
        cursor: SharedFlow<AtCursor>,
        limit: Long = 50,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem.ProfileItem>>> = flow {
        val uri = AtUri.followersUri(id)
        cursor.collect { cur ->
            val prev = dataFlows[uri]?.value
            val query = GetFollowersQuery(id, limit, cur)
            api.api.getFollowers(query).onSuccess { response ->
                val data = if (cur != null && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(prev, response.followers.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) })
                } else if (cur == null && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(response.followers.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) }, prev)
                } else {
                    MorphoData("Following", uri, response.cursor,
                               response.followers.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) })
                }.copy(query = json.encodeToJsonElement(query))
                emit(Result.success(data as MorphoData<MorphoDataItem.ProfileItem>))
                mutex.withLock {
                    if(prev == null) _dataFlows[uri] = MutableStateFlow(data as MorphoData<MorphoDataItem>)
                    else _dataFlows[uri]?.update { data as MorphoData<MorphoDataItem> }
                }
            }.onFailure {
                emit(Result.failure(it))
                log.e { "Failed to get followers for $id.\nError: $it" }
                log.v { "Cursor: $cur | Limit: $limit" }
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("Followers of $id"))
    suspend fun authorFeed(
        id: AtIdentifier,
        type: FeedType,
        cursor: SharedFlow<AtCursor>,
        limit: Long = 50,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem.FeedItem>>> = flow<Result<MorphoData<MorphoDataItem.FeedItem>>> {
        when(type){
            FeedType.PROFILE_POSTS -> {
                val uri = AtUri.profilePostsUri(id)
                cursor.collect { cur ->
                        val prev = dataFlows[uri]?.value
                        val query = GetAuthorFeedQuery(id, limit, cur, GetAuthorFeedFilter.POSTS_NO_REPLIES)
                        api.api.getAuthorFeed(query).onSuccess { response ->
                            val newPosts = MorphoDataFeed
                                .collectThreads(api, response.cursor, response.feed.toBskyPostList()).last()
                            val feed = if (cur != null && prev != null && prev.items.isNotEmpty()) {
                                MorphoDataFeed.concat(prev, newPosts)
                            } else if (cur == null && prev != null && prev.items.isNotEmpty()) {
                                MorphoDataFeed.concat(newPosts, prev)
                            } else {
                                newPosts
                            }
                            val data = feed.toMorphoData("Posts")
                                .copy(query = json.encodeToJsonElement(query))
                            emit(Result.success(data as MorphoData<MorphoDataItem.FeedItem>))
                            mutex.withLock {
                                if(prev == null) _dataFlows[uri] = MutableStateFlow(data)
                                else _dataFlows[uri]?.update { data }
                            }
                        }.onFailure {
                            emit(Result.failure(it))
                            log.e { "Failed to get posts feed for $id.\nError: $it" }
                            log.v { "Cursor: $cur | Limit: $limit\n" }
                        }
                    }
            }
            FeedType.PROFILE_REPLIES -> {
                val uri = AtUri.profileRepliesUri(id)
                cursor.collect { cur ->
                    val prev = dataFlows[uri]?.value
                    val query = GetAuthorFeedQuery(id, limit, cur, GetAuthorFeedFilter.POSTS_WITH_REPLIES)
                    api.api.getAuthorFeed(query).onSuccess { response ->
                        val newPosts = MorphoDataFeed
                            .collectThreads(api, response.cursor, response.feed.toBskyPostList()).last()
                        val feed = if (cur != null && prev != null && prev.items.isNotEmpty()) {
                            MorphoDataFeed.concat(prev, newPosts)
                        } else if (cur == null && prev != null && prev.items.isNotEmpty()) {
                            MorphoDataFeed.concat(newPosts, prev)
                        } else {
                            newPosts
                        }
                        val data = feed.toMorphoData("Replies")
                            .copy(query = json.encodeToJsonElement(query))
                        emit(Result.success(data as MorphoData<MorphoDataItem.FeedItem>))
                        mutex.withLock {
                            if(prev == null) _dataFlows[uri] = MutableStateFlow(data)
                            else _dataFlows[uri]?.update { data }
                        }
                    }.onFailure {
                        emit(Result.failure(it))
                        log.e { "Failed to get reply feed of $id.\nError: $it" }
                        log.v { "Cursor: $cur | Limit: $limit\n" }
                    }
                }
            }
            FeedType.PROFILE_MEDIA -> {
                val uri = AtUri.profileMediaUri(id)
                cursor.onEach { cur ->
                    val prev = dataFlows[uri]?.value
                    val query = GetAuthorFeedQuery(id, limit, cur, GetAuthorFeedFilter.POSTS_WITH_MEDIA)
                    api.api.getAuthorFeed(query).onSuccess { response ->
                        val newPosts = MorphoDataFeed
                            .collectThreads(api, response.cursor, response.feed.toBskyPostList()).last()
                        val feed = if (cur != null && prev != null && prev.items.isNotEmpty()) {
                            MorphoDataFeed.concat(prev, newPosts)
                        } else if (cur == null && prev != null && prev.items.isNotEmpty()) {
                            MorphoDataFeed.concat(newPosts, prev)
                        } else {
                            newPosts
                        }
                        val data = feed.toMorphoData("Media")
                            .copy(query = json.encodeToJsonElement(query))
                        emit(Result.success(data as MorphoData<MorphoDataItem.FeedItem>))
                        mutex.withLock {
                            if(prev == null) _dataFlows[uri] = MutableStateFlow(data)
                            else _dataFlows[uri]?.update { data }
                        }
                    }.onFailure {
                        emit(Result.failure(it))
                        log.e { "Failed to get media feed of $id.\nError: $it" }
                        log.v { "Cursor: $cur | Limit: $limit\n" }
                    }
                }
            }
            else -> {
                emit(Result.failure(Exception("Invalid profile tab type.")))
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("${type.name} feed for $id"))
    suspend fun profileTabContent(
        id: AtIdentifier,
        type: FeedType,
        cursor: SharedFlow<AtCursor>,
        limit: Long = 50,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem>>> = flow {
        when(type) {
            FeedType.PROFILE_FEEDS_LIST -> {
                profileFeedsList(id, cursor, limit, dispatcher, scope)
                    .onEach { emit(it as Result<MorphoData<MorphoDataItem>>) }
            }
            FeedType.PROFILE_USER_LISTS -> {
                profileLists(id, cursor, limit, dispatcher, scope)
                    .onEach { emit(it as Result<MorphoData<MorphoDataItem>>) }
            }
            FeedType.PROFILE_LIKES -> {
                profileLikes(id, cursor, limit, dispatcher, scope)
                    .onEach { emit(it as Result<MorphoData<MorphoDataItem>>) }
            }
            FeedType.PROFILE_MOD_SERVICE -> {
                if (id.toString().startsWith("did:"))
                    profileServiceView(Did(id.toString()), cursor.map { Unit }.shareIn(scope, SharingStarted.Lazily), dispatcher, scope)
                    .onEach { emit(it as Result<MorphoData<MorphoDataItem>>) }
            }
            else -> {
                authorFeed(id, type, cursor, limit, dispatcher, scope)
                    .onEach { emit(it as Result<MorphoData<MorphoDataItem>>) }
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("${type.name} content for $id"))
    suspend fun profileLists(
        id: AtIdentifier,
        cursor: SharedFlow<AtCursor>,
        limit: Long = 50,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem.ListInfo>>> = flow<Result<MorphoData<MorphoDataItem.ListInfo>>> {
        val uri = AtUri.profileUserListsUri(id)
        cursor.collect { cur ->
            val prev = dataFlows[uri]?.value
            val query = GetListsQuery(id, limit, cur)
            api.api.getLists(query).onSuccess { response ->
                val data = if (cur != null && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(prev, response.lists.mapImmutable { MorphoDataItem.ListInfo(it.toList()) })
                } else if (cur == null && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(response.lists.mapImmutable { MorphoDataItem.ListInfo(it.toList()) }, prev)
                } else {
                    MorphoData("Lists", uri, response.cursor,
                               response.lists.mapImmutable { MorphoDataItem.ListInfo(it.toList()) })
                }.copy(query = json.encodeToJsonElement(query))

                emit(Result.success(data as MorphoData<MorphoDataItem.ListInfo>))
                mutex.withLock {
                    if(prev == null) _dataFlows[uri] = MutableStateFlow(data as MorphoData<MorphoDataItem>)
                    else _dataFlows[uri]?.update { data as MorphoData<MorphoDataItem> }
                }
            }.onFailure {
                emit(Result.failure(it))
                log.e { "Failed to get lists for $id.\nError: $it" }
                log.v { "Cursor: $cur | Limit: $limit" }
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("Lists made by $id"))
    suspend fun profileFeedsList(
        id: AtIdentifier,
        cursor: SharedFlow<AtCursor>,
        limit: Long = 50,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem.FeedInfo>>> = flow<Result<MorphoData<MorphoDataItem.FeedInfo>>> {
        val uri = AtUri.profileFeedsListUri(id)
        cursor.onEach { cur ->
            val prev = dataFlows[uri]?.value
            val query = GetActorFeedsQuery(id, limit, cur)
            api.api.getActorFeeds(query).onSuccess { response ->
                val data = if (cur != null && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(prev, response.feeds.mapImmutable { MorphoDataItem.FeedInfo(it.toFeedGenerator()) })
                } else if (cur == null && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(response.feeds.mapImmutable { MorphoDataItem.FeedInfo(it.toFeedGenerator()) }, prev)
                } else {
                    MorphoData("Feeds", uri, response.cursor,
                               response.feeds.mapImmutable { MorphoDataItem.FeedInfo(it.toFeedGenerator()) })
                }.copy(query = json.encodeToJsonElement(query))

                emit(Result.success(data as MorphoData<MorphoDataItem.FeedInfo>))
                mutex.withLock {
                    if(prev == null) _dataFlows[uri] = MutableStateFlow(data as MorphoData<MorphoDataItem>)
                    else _dataFlows[uri]?.update { data as MorphoData<MorphoDataItem> }
                }
            }.onFailure {
                emit(Result.failure(it))
                log.e { "Failed to get feeds for $id.\nError: $it" }
                log.v { "Cursor: $cur | Limit: $limit" }
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("Feeds made by $id"))
    suspend fun profileServiceView(
        did: Did,
        update: SharedFlow<Unit>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem.LabelService>>> = flow<Result<MorphoData<MorphoDataItem.LabelService>>> {
        val uri = AtUri.profileModServiceUri(did)
        update.collect {
            val query = GetServicesQuery(listOf(did).toImmutableList(), true)
            api.api.getServices(query).onSuccess { response ->
                val data = MorphoData("Labels", uri, null,
                      response.views.mapImmutable {
                          when(it) {
                              is GetServicesResponseViewUnion.LabelerViewDetailed ->
                                  MorphoDataItem.LabelService(it.value.toLabelService())
                              is GetServicesResponseViewUnion.LabelerView ->
                                  MorphoDataItem.LabelService(it.value.toLabelService())
                          }
                      })

                emit(Result.success(data))
                mutex.withLock {
                    if(dataFlows[uri] == null) _dataFlows[uri] = MutableStateFlow(data as MorphoData<MorphoDataItem>)
                    else _dataFlows[uri]?.update { data as MorphoData<MorphoDataItem> }
                }
            }.onFailure {
                emit(Result.failure(it))
                log.e { "Failed to get label services for $did.\nError: $it" }
            }
        }
    }.distinctUntilChanged()
        .flowOn(dispatcher + CoroutineName("Label Services of $did"))

    suspend fun profileLikes(
        id: AtIdentifier,
        cursor: SharedFlow<AtCursor>,
        limit: Long = 50,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem.FeedItem>>> = flow<Result<MorphoData<MorphoDataItem.FeedItem>>> {
        val uri = AtUri.profileUserListsUri(id)
        cursor.collect { cur ->
            val prev = dataFlows[uri]?.value

            val query = GetActorLikesQuery(id, limit, cur)
            api.api.getActorLikes(query) .onSuccess { response ->
                val newPosts = MorphoDataFeed
                    .collectThreads(api, response.cursor, response.feed.toBskyPostList()).last()
                val feed = if (cur != null && prev != null && prev.items.isNotEmpty()) {
                    MorphoDataFeed.concat(prev, newPosts)
                } else if (cur == null && prev != null && prev.items.isNotEmpty()) {
                    MorphoDataFeed.concat(newPosts, prev)
                } else {
                    newPosts
                }
                val data = feed.toMorphoData("Likes").copy(query = json.encodeToJsonElement(query))

                emit(Result.success(data as MorphoData<MorphoDataItem.FeedItem>))
                mutex.withLock {
                    if(prev == null) _dataFlows[uri] = MutableStateFlow(data)
                    else _dataFlows[uri]?.update { data }
                }
            }.onFailure {
                emit(Result.failure(it))
                log.e { "Failed to get likes for $id.\nError: $it" }
                log.v { "Cursor: $cur | Limit: $limit" }
            }

        }
    }.distinctUntilChanged()
        .flowOn(dispatcher + CoroutineName("Likes of $id"))

    suspend fun profiles(
        profiles: ImmutableList<AtIdentifier>,
        update: SharedFlow<Unit>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = serviceScope,
    ): Flow<Result<MorphoData<MorphoDataItem.ProfileItem>>> = flow<Result<MorphoData<MorphoDataItem.ProfileItem>>> {
        val uri = AtUri.myUserListUri(profiles.hashCode().toString())
        update.collect {
            val query = GetProfilesQuery(profiles)
            api.api.getProfiles(query).onSuccess { response ->

                val data = MorphoData("Profiles", uri, null,
                                      response.profiles.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) },
                                              json.encodeToJsonElement(query))

                emit(Result.success(data))
                mutex.withLock {
                    if(dataFlows[uri] == null) _dataFlows[uri] = MutableStateFlow(data as MorphoData<MorphoDataItem>)
                    else _dataFlows[uri]?.update { data as MorphoData<MorphoDataItem> }
                }
            }.onFailure {
                emit(Result.failure(it))
                log.e { "Failed to get profiles.\nError: $it" }
                log.v { "$profiles" }
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher)
    suspend fun <T:MorphoDataItem> peekLatest(
        feed: MorphoData<T>,
        update: SharedFlow<Unit> = MutableSharedFlow<Unit>(),
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        scope: CoroutineScope = serviceScope,
    ): Flow<MorphoDataItem?> = flow {
        update.collect {
            when(feed.feedType) {
                FeedType.HOME -> {
                    val query = GetTimelineQuery(limit = 1, cursor = null)
                    api.api.getTimeline(query).onSuccess { response ->
                        if (response.feed.isNotEmpty()) {
                            val cid = response.feed.first().post.cid
                            if (!feed.contains(cid)) {
                                emit(MorphoDataItem.Post(response.feed.first().toPost()))
                            } else {
                                emit(null)
                            }
                        }
                    }.onFailure { emit(null) }
                }
                FeedType.PROFILE_POSTS -> {
                    val query = GetAuthorFeedQuery(feed.uri.id(api), 1, null, GetAuthorFeedFilter.POSTS_NO_REPLIES)
                    api.api.getAuthorFeed(query)
                        .onSuccess { response ->
                            if (response.feed.isNotEmpty()) {
                                val cid = response.feed.first().post.cid
                                if (!feed.contains(cid)) {
                                    emit(MorphoDataItem.Post(response.feed.first().toPost()))
                                } else {
                                    emit(null)
                                }
                            }
                        }.onFailure { emit(null) }
                }
                FeedType.PROFILE_REPLIES -> {
                    val query = GetAuthorFeedQuery(feed.uri.id(api), 1, null, GetAuthorFeedFilter.POSTS_WITH_REPLIES)
                    api.api.getAuthorFeed(query)
                        .onSuccess { response ->
                            if (response.feed.isNotEmpty()) {
                                val cid = response.feed.first().post.cid
                                if (!feed.contains(cid)) {
                                    emit(MorphoDataItem.Post(response.feed.first().toPost()))
                                } else {
                                    emit(null)
                                }
                            }
                        }.onFailure { emit(null) }
                }
                FeedType.PROFILE_MEDIA -> {
                    val query = GetAuthorFeedQuery(feed.uri.id(api), 1, null, GetAuthorFeedFilter.POSTS_WITH_MEDIA)
                    api.api.getAuthorFeed(query)
                        .onSuccess { response ->
                            if (response.feed.isNotEmpty()) {
                                val cid = response.feed.first().post.cid
                                if (!feed.contains(cid)) {
                                    emit(MorphoDataItem.Post(response.feed.first().toPost()))
                                } else {
                                    emit(null)
                                }
                            }
                        }.onFailure { emit(null) }
                }
                FeedType.PROFILE_LIKES -> {
                    val query = GetActorLikesQuery(feed.uri.id(api), 1, null)
                    api.api.getActorLikes(query)
                        .onSuccess { response ->
                            if (response.feed.isNotEmpty()) {
                                val cid = response.feed.first().post.cid
                                if (!feed.contains(cid)) {
                                    emit(MorphoDataItem.Post(response.feed.first().toPost()))
                                } else {
                                    emit(null)
                                }
                            }
                        }.onFailure { emit(null) }
                }
                FeedType.PROFILE_USER_LISTS -> {
                    val query = GetListsQuery(feed.uri.id(api), 1)
                    api.api.getLists(query)
                        .onSuccess { response ->
                            if (response.lists.isNotEmpty()) {
                                val cid = response.lists.first().cid
                                if (!feed.contains(cid)) {
                                    emit(MorphoDataItem.ListInfo(response.lists.first().toList()))
                                } else {
                                    emit(null)
                                }
                            }
                        }.onFailure { emit(null) }
                }
                FeedType.PROFILE_MOD_SERVICE -> {
                    val id = feed.uri.id(api)
                    if(Did.Regex.matches(id.toString())) emit(null)
                    else {
                        val query = GetServicesQuery(persistentListOf(Did(id.toString())), true)
                        api.api.getServices(query)
                            .onSuccess { response ->
                                if (response.views.isNotEmpty()) {
                                    when(response.views.first()) {
                                        is GetServicesResponseViewUnion.LabelerViewDetailed -> {
                                            val cid = (response.views.first() as GetServicesResponseViewUnion.LabelerViewDetailed).value.cid
                                            if (!feed.contains(cid)) {
                                                emit(MorphoDataItem.LabelService((response.views.first() as GetServicesResponseViewUnion.LabelerViewDetailed).value.toLabelService()))
                                            } else {
                                                emit(null)
                                            }
                                        }

                                        is GetServicesResponseViewUnion.LabelerView -> {
                                            val cid = (response.views.first() as GetServicesResponseViewUnion.LabelerView).value.cid
                                            if (!feed.contains(cid)) {
                                                emit(MorphoDataItem.LabelService((response.views.first() as GetServicesResponseViewUnion.LabelerView).value.toLabelService()))
                                            } else {
                                                emit(null)
                                            }
                                        }
                                    }
                                }
                            }.onFailure { emit(null) }
                    }
                }
                FeedType.PROFILE_FEEDS_LIST -> {
                    val query = GetActorFeedsQuery(feed.uri.id(api), 1)
                    api.api.getActorFeeds(query)
                        .onSuccess { response ->
                            if (response.feeds.isNotEmpty()) {
                                val cid = response.feeds.first().cid
                                if (!feed.contains(cid)) {
                                    emit(MorphoDataItem.FeedInfo(response.feeds.first().toFeedGenerator()))
                                } else {
                                    emit(null)
                                }
                            }
                        }.onFailure { emit(null) }
                }
                FeedType.OTHER -> {
                    // assume it's a custom feed for now, but we should probably add more types
                    val query = GetFeedQuery(feed.uri, 1)
                    api.api.getFeed(query)
                        .onSuccess { response ->
                            if (response.feed.isNotEmpty()) {
                                val cid = response.feed.first().post.cid
                                if (!feed.contains(cid)) {
                                    emit(MorphoDataItem.Post(response.feed.first().toPost()))
                                } else {
                                    emit(null)
                                }
                            }
                        }.onFailure { emit(null) }
                }
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher)

    fun removeFeed(uri: AtUri): MorphoData<MorphoDataItem>?  {
        return _dataFlows.remove(uri)?.value
    }
}