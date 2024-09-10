package com.morpho.app.model.uidata

//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import app.bsky.actor.GetProfilesQuery
import app.bsky.actor.ProfileViewBasic
import app.bsky.feed.*
import app.bsky.graph.GetFollowersQuery
import app.bsky.graph.GetFollowsQuery
import app.bsky.graph.GetListsQuery
import app.bsky.labeler.GetServicesQuery
import app.bsky.labeler.GetServicesResponseViewUnion
import com.atproto.repo.GetRecordQuery
import com.atproto.repo.StrongRef
import com.morpho.app.di.UpdateTick
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.ContentLoadingState
import com.morpho.app.model.uistate.FeedType
import com.morpho.app.util.json
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.*
import kotlinx.collections.immutable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.mp.KoinPlatform.getKoin
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

suspend fun getPost(uri: AtUri, api: Butterfly = getKoin().get<Butterfly>()): Flow<BskyPost?> = flow {
    val query = GetPostsQuery(persistentListOf(uri))
    api.api.getPosts(query).onSuccess { response ->
        emit(response.posts.firstOrNull()?.toPost())
    }.onFailure {
        BskyDataService.log.e { "Failed to get post at $uri.\nError: $it" }
        emit(null)
    }
}

fun getReplyRefs(uri: AtUri, api: Butterfly = getKoin().get<Butterfly>()): Flow<Result<PostReplyRef>> = flow {
    uri.toParts().onFailure { emit(Result.failure(it)) }.onSuccess { uriParts->
        api.api.getRecord(GetRecordQuery(uriParts.repo, uriParts.collection, uriParts.rkey))
            .onSuccess { parentResponse ->
                val parentReply = parentResponse.value.jsonObject["reply"]?.jsonObject
                if(parentReply != null) {
                    val rootUri = parentReply["root"]?.jsonObject?.get("uri")?.recordType
                    if (rootUri != null) {
                        AtUri.parseAtUri(rootUri).onFailure { emit(Result.failure(it)) }.onSuccess { parts ->
                            api.api.getRecord(GetRecordQuery(parts.repo, parts.collection, parts.rkey))
                                .onSuccess { rootResponse ->
                                    val rootRef = rootResponse.cid?.let { StrongRef(rootResponse.uri, it) }
                                    val parentRef = parentResponse.cid?.let { StrongRef(parentResponse.uri, it) }
                                    val grandParentAuthor = parentReply["grandparentAuthor"]?.jsonObject?.let { ProfileViewBasic.serializer().deserialize(it) }
                                    if(rootRef != null && parentRef != null) {
                                        emit(Result.success(PostReplyRef(rootRef, parentRef, grandParentAuthor)))
                                    } else {
                                        emit(Result.failure(Error(
                                            "Failed to get reply refs:\nRoot: $rootResponse\nParent: $parentResponse")))
                                    }
                                }.onFailure { emit(Result.failure(it)) }
                        }

                    }
                }
            }.onFailure { emit(Result.failure(it)) }
    }

}

suspend fun getPosts(posts: List<AtUri>, api: Butterfly = getKoin().get<Butterfly>()): Flow<List<BskyPost>?> = flow {
    val query = GetPostsQuery(posts.toPersistentList())
    api.api.getPosts(query).onSuccess { response ->
        emit(response.posts.mapImmutable { it.toPost() })
    }.onFailure {
        BskyDataService.log.e { "Failed to get post.\nError: $it" }
        emit(null)
    }
}

@Suppress("unused", "MemberVisibilityCanBePrivate", "UNCHECKED_CAST")
// TODO: Revisit these casts if we can, but they should be safe
@Serializable
class BskyDataService: KoinComponent {
    val api: Butterfly by inject()

    private val _dataFlows = mutableMapOf<AtUri, MutableStateFlow<MorphoData<MorphoDataItem>>>()
    val useFeedTuners: (MorphoData<MorphoDataItem.FeedItem>) -> List<FeedTuner> = { feed ->
        settings.currentUserPrefs.value?.let { FeedTuner.useFeedTuners(it, feed) } ?: listOf(FeedTuner())
    }
    private val mutex = Mutex()
    private val contentLabelService by inject<ContentLabelService>()
    private val settings: SettingsService by inject<SettingsService>()
    private val languages: StateFlow<List<Language>> = settings.languages
        .stateIn(serviceScope, SharingStarted.Lazily, persistentListOf())


    // Secondary way to make sure you have the most recent stuff, in case you lose the original reference
    val dataFlows: ImmutableMap<AtUri, StateFlow<MorphoData<MorphoDataItem>>>
            get() = _dataFlows.mapValues { it.value.asStateFlow() }.toImmutableMap()

    companion object {
        val log = logging()
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    suspend fun refresh(
        uri: AtUri,
        cursor: AtCursor = AtCursor.EMPTY,
    ): Result<StateFlow<MorphoData<MorphoDataItem>>> {
        val flow = dataFlows[uri] ?: return Result.failure(Exception("No feed to refresh."))
        val data = flow.value
        when(data.feedType) {
            FeedType.HOME -> {
                try {
                    val query = json.decodeFromJsonElement<GetTimelineQuery>(data.query).copy(cursor = cursor.cursor)
                    api.api.getTimeline(query).onSuccess { response ->
                        if (response.cursor == cursor.cursor && cursor != AtCursor.EMPTY) {
                            return@onSuccess
                        }
                        val new = MorphoData.concatFeed(
                            query = json.encodeToJsonElement(query),
                            responseCursor = response.cursor,
                            oldCursor = cursor,
                            feed = response.feed,
                            data = data as MorphoData<MorphoDataItem.FeedItem>,
                        ).collectThreads(api = api).single()
                        var tunedFeed = new
                        useFeedTuners(tunedFeed).forEach { tuner ->
                            tunedFeed = tuner.tune(tunedFeed)
                        }
                        mutex.withLock {
                            _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
                    val query = json.decodeFromJsonElement<GetAuthorFeedQuery>(data.query).copy(cursor = cursor.cursor)
                    api.api.getAuthorFeed(query).onSuccess { response ->
                        if (response.cursor == cursor.cursor && cursor != AtCursor.EMPTY) {
                            return@onSuccess
                        }
                        var tunedFeed = MorphoData.concatFeed(
                            query = json.encodeToJsonElement(query),
                            responseCursor = response.cursor,
                            oldCursor = cursor,
                            feed = response.feed,
                            data = data as MorphoData<MorphoDataItem.FeedItem>,
                            title = "Posts",
                        ).collectThreads(api = api).single()
                        useFeedTuners(tunedFeed).forEach { tuner ->
                            tunedFeed = tuner.tune(tunedFeed)
                        }
                        mutex.withLock {
                            _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
                    val query = Json.decodeFromJsonElement<GetAuthorFeedQuery>(data.query).copy(cursor = cursor.cursor)
                    api.api.getAuthorFeed(query).onSuccess { response ->
                        if (response.cursor == cursor.cursor && cursor != AtCursor.EMPTY) {
                            return@onSuccess
                        }
                        var tunedFeed = MorphoData.concatFeed(
                            query = json.encodeToJsonElement(query),
                            responseCursor = response.cursor,
                            oldCursor = cursor,
                            feed = response.feed,
                            data = data as MorphoData<MorphoDataItem.FeedItem>,
                            title = "Replies",
                        ).collectThreads(api = api).single()
                        useFeedTuners(tunedFeed).forEach { tuner ->
                            tunedFeed = tuner.tune(tunedFeed)
                        }
                        mutex.withLock {
                            _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
                    val query = json.decodeFromJsonElement<GetAuthorFeedQuery>(data.query).copy(cursor = cursor.cursor)
                    api.api.getAuthorFeed(query).onSuccess { response ->
                        if (response.cursor == cursor.cursor && cursor != AtCursor.EMPTY) {
                            return@onSuccess
                        }
                        var tunedFeed = MorphoData.concatFeed(
                            query = json.encodeToJsonElement(query),
                            responseCursor = response.cursor,
                            oldCursor = cursor,
                            feed = response.feed,
                            data = data as MorphoData<MorphoDataItem.FeedItem>,
                            title = "Media",
                        )
                        useFeedTuners(tunedFeed).forEach { tuner ->
                            tunedFeed = tuner.tune(tunedFeed)
                        }
                        mutex.withLock {
                            _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
                    val query = json.decodeFromJsonElement<GetActorLikesQuery>(data.query).copy(cursor = cursor.cursor)
                    api.api.getActorLikes(query).onSuccess { response ->
                        if (response.cursor == cursor.cursor && cursor != AtCursor.EMPTY) {
                            return@onSuccess
                        }
                        var tunedFeed = MorphoData.concatFeed(
                            query = json.encodeToJsonElement(query),
                            responseCursor = response.cursor,
                            oldCursor = cursor,
                            feed = response.feed,
                            data = data as MorphoData<MorphoDataItem.FeedItem>,
                            title = "Likes",
                        )
                        useFeedTuners(tunedFeed).forEach { tuner ->
                            tunedFeed = tuner.tune(tunedFeed)
                        }
                        mutex.withLock {
                            _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
                    val query = json.decodeFromJsonElement<GetListsQuery>(data.query).copy(cursor = cursor.cursor)
                    api.api.getLists(query).onSuccess { response ->
                        if (response.cursor == cursor.cursor && cursor != AtCursor.EMPTY) {
                            return@onSuccess
                        }
                        val newData = if (cursor != AtCursor.EMPTY && data.items.isNotEmpty()) {
                            MorphoData.concat(data, response.lists.mapImmutable { MorphoDataItem.ListInfo(it.toList()) })
                        } else if (cursor == AtCursor.EMPTY && data.items.isNotEmpty()) {
                            MorphoData.concat(response.lists.mapImmutable { MorphoDataItem.ListInfo(it.toList()) }, data)
                        } else {
                            MorphoData("Lists", uri, AtCursor(response.cursor, cursor.scroll),
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
                    val query = json.decodeFromJsonElement<GetServicesQuery>(data.query)
                    api.api.getServices(query).onSuccess { response ->

                        val newData = if (cursor != AtCursor.EMPTY && data.items.isNotEmpty()) {
                            MorphoData.concat(data, response.views.mapImmutable {
                                when(it) {
                                    is GetServicesResponseViewUnion.LabelerViewDetailed ->
                                        MorphoDataItem.LabelService(it.value.toLabelService())
                                    is GetServicesResponseViewUnion.LabelerView ->
                                        MorphoDataItem.LabelService(it.value.toLabelService())
                                }
                            })
                        } else if (cursor == AtCursor.EMPTY && data.items.isNotEmpty()) {
                            MorphoData.concat(response.views.mapImmutable {
                                when(it) {
                                    is GetServicesResponseViewUnion.LabelerViewDetailed ->
                                        MorphoDataItem.LabelService(it.value.toLabelService())
                                    is GetServicesResponseViewUnion.LabelerView ->
                                        MorphoDataItem.LabelService(it.value.toLabelService())
                                }
                            }, data)
                        } else {
                            MorphoData("Services", uri, AtCursor.EMPTY,
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
                    val query = json.decodeFromJsonElement<GetActorFeedsQuery>(data.query).copy(cursor = cursor.cursor)
                    api.api.getActorFeeds(query).onSuccess { response ->
                        if (response.cursor == cursor.cursor && cursor != AtCursor.EMPTY) {
                            return@onSuccess
                        }
                        val newData = if (cursor != AtCursor.EMPTY && data.items.isNotEmpty()) {
                            MorphoData.concat(data, response.feeds.mapImmutable { MorphoDataItem.FeedInfo(it.toFeedGenerator()) })
                        } else if (cursor == AtCursor.EMPTY && data.items.isNotEmpty()) {
                            MorphoData.concat(response.feeds.mapImmutable { MorphoDataItem.FeedInfo(it.toFeedGenerator()) }, data)
                        } else {
                            MorphoData("Feeds", uri, AtCursor(response.cursor, cursor.scroll),
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
                    val query = json.decodeFromJsonElement<GetFeedQuery>(data.query).copy(cursor = cursor.cursor)
                    api.api.getFeed(query).onSuccess { response ->
                        if (response.cursor == cursor.cursor && cursor != AtCursor.EMPTY) {
                            return@onSuccess
                        }
                        var tunedFeed = MorphoData.concatFeed(
                            query = json.encodeToJsonElement(query),
                            responseCursor = response.cursor,
                            oldCursor = cursor,
                            feed = response.feed,
                            data = data as MorphoData<MorphoDataItem.FeedItem>,
                        ).collectThreads(api = api).single()
                        useFeedTuners(tunedFeed).forEach { tuner ->
                            tunedFeed = tuner.tune(tunedFeed)
                        }
                        mutex.withLock {
                            _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
                        }
                        return Result.success(flow)
                    }
                } catch (e: Exception) {
                    log.e { "Failed to refresh feed at $uri.\nError: $e" }
                    return Result.failure(e)
                }
            }
            FeedType.LIST_FOLLOWING -> {
                try {
                    val query = json.decodeFromJsonElement<GetListFeedQuery>(data.query).copy(cursor = cursor.cursor)
                    api.api.getListFeed(query).onSuccess { response ->
                        if (response.cursor == cursor.cursor && cursor != AtCursor.EMPTY) {
                            return@onSuccess
                        }
                        var tunedFeed = MorphoData.concatFeed(
                            query = json.encodeToJsonElement(query),
                            responseCursor = response.cursor,
                            oldCursor = cursor,
                            feed = response.feed,
                            data = data as MorphoData<MorphoDataItem.FeedItem>,
                        ).collectThreads(api = api).single()
                        useFeedTuners(tunedFeed).forEach { tuner ->
                            tunedFeed = tuner.tune(tunedFeed)
                        }
                        mutex.withLock {
                            _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
    ): Flow<Result<MorphoData<MorphoDataItem.FeedItem>>> = flow {
        cursor.debounce(300).combine(feedPref) { c, f -> c to f }
            .collect { flows ->
                //log.d { "Timeline flow tick." }
                val (cur, pref) = flows
                val prev = dataFlows[AtUri.HOME_URI]?.value
                val query = GetTimelineQuery(limit = limit, cursor = cur.cursor)
                api.api.getTimeline(query).onSuccess { response ->
                    if (response.cursor == cur.cursor && cur != AtCursor.EMPTY) {
                        return@collect
                    }
                    var tunedFeed = MorphoData.concatFeed(
                        query = json.encodeToJsonElement(query),
                        responseCursor = response.cursor,
                        oldCursor = cur,
                        feed = response.feed,
                        data = (prev ?: MorphoData.EMPTY()) as MorphoData<MorphoDataItem.FeedItem>,
                        title = "Home",
                        uri = AtUri.HOME_URI,
                    ).collectThreads(api = api).single()
                    useFeedTuners(tunedFeed).forEach { tuner ->
                        tunedFeed = tuner.tune(tunedFeed)
                    }
                    emit(Result.success(tunedFeed))
                    log.d{
                        "Timeline " +
                        "Old cursor: $cur " +
                        "New cursor: ${response.cursor}"
                    }
                    log.v {
                        "${tunedFeed.items.map {
                            when(it) {
                                is MorphoDataItem.Post -> "${it.post.uri}\n"
                                is MorphoDataItem.Thread -> "${it.thread.post.uri}\n"
                            }
                        }}"
                    }
                    mutex.withLock {
                        if(prev == null) _dataFlows[AtUri.HOME_URI] = MutableStateFlow(tunedFeed as MorphoData<MorphoDataItem>)
                        else _dataFlows[AtUri.HOME_URI]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
    ): Flow<Result<MorphoData<MorphoDataItem.FeedItem>>> = flow {
        cursor.debounce(300).combine(feedPref) { c, f -> c to f }
            .collect { flows ->
                //log.d { "Feed flow tick."}
                val cur = flows.first
                val pref = flows.second
                val prev = dataFlows[feedInfo.uri]?.value
                val query = GetFeedQuery(feedInfo.uri, limit, cur.cursor)
                api.api.getFeed(query).onSuccess { response ->
                    if (response.cursor == cur.cursor) {
                        return@collect
                    }
                    var tunedFeed = MorphoData.concatFeed(
                        query = json.encodeToJsonElement(query),
                        responseCursor = response.cursor,
                        oldCursor = cur,
                        feed = response.feed,
                        data = (prev ?: MorphoData.EMPTY()) as MorphoData<MorphoDataItem.FeedItem>,
                        title = feedInfo.name,
                        uri = feedInfo.uri,
                    ).collectThreads(api = api).single()
                    useFeedTuners(tunedFeed).forEach { tuner ->
                        tunedFeed = tuner.tune(tunedFeed)
                    }
                    emit(Result.success(tunedFeed))
                    log.d{
                        "Feed: ${feedInfo.name} " +
                        "Old cursor: $cur " +
                        "New cursor: ${response.cursor}"
                    }
                    log.v {
                        "${tunedFeed.items.map {
                            when(it) {
                                is MorphoDataItem.Post -> it.post.uri
                                is MorphoDataItem.Thread -> it.thread.post.uri
                            }
                        }}"
                    }
                    mutex.withLock {
                        if(prev == null) _dataFlows[feedInfo.uri] = MutableStateFlow(tunedFeed as MorphoData<MorphoDataItem>)
                        else _dataFlows[feedInfo.uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
    ): Flow<Result<MorphoData<MorphoDataItem.ProfileItem>>> = flow {
        val uri = AtUri.followsUri(id)
        cursor.collect { cur ->
            val prev = dataFlows[uri]?.value
            val query = GetFollowsQuery(id, limit, cur.cursor)
            api.api.getFollows(query).onSuccess { response ->
                if (response.cursor == cur.cursor) {
                    return@collect
                }
                val data = if (cur != AtCursor.EMPTY && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(prev, response.follows.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) })
                } else if (cur == AtCursor.EMPTY && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(response.follows.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) }, prev)
                } else {
                    MorphoData("Following", uri, AtCursor(response.cursor, cur.scroll),
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
    ): Flow<Result<MorphoData<MorphoDataItem.ProfileItem>>> = flow {
        val uri = AtUri.followersUri(id)
        cursor.collect { cur ->
            val prev = dataFlows[uri]?.value
            val query = GetFollowersQuery(id, limit, cur.cursor)
            api.api.getFollowers(query).onSuccess { response ->
                if (response.cursor == cur.cursor) {
                    return@collect
                }
                val data = if (cur != AtCursor.EMPTY && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(prev, response.followers.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) })
                } else if (cur == AtCursor.EMPTY && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(response.followers.mapImmutable { MorphoDataItem.ProfileItem(it.toProfile()) }, prev)
                } else {
                    MorphoData("Following", uri, AtCursor(response.cursor, cur.scroll),
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
    ): Flow<Result<MorphoData<MorphoDataItem.FeedItem>>> = flow<Result<MorphoData<MorphoDataItem.FeedItem>>> {
        when(type){
            FeedType.PROFILE_POSTS -> {
                val uri = AtUri.profilePostsUri(id)
                cursor.collect { cur ->
                        val prev = dataFlows[uri]?.value
                        val query = GetAuthorFeedQuery(id, limit, cur.cursor, GetAuthorFeedFilter.POSTS_NO_REPLIES)
                        api.api.getAuthorFeed(query).onSuccess { response ->
                            if (response.cursor == cur.cursor) {
                                return@collect
                            }
                            var tunedFeed = MorphoData.concatFeed(
                                query = json.encodeToJsonElement(query),
                                responseCursor = response.cursor,
                                oldCursor = cur,
                                feed = response.feed,
                                data = (prev ?: MorphoData.EMPTY()) as MorphoData<MorphoDataItem.FeedItem>,
                                title = "Posts",
                            ).collectThreads(api = api).single()
                            useFeedTuners(tunedFeed).forEach { tuner ->
                                tunedFeed = tuner.tune(tunedFeed)
                            }
                            emit(Result.success(tunedFeed))
                            mutex.withLock {
                                if(prev == null) _dataFlows[uri] = MutableStateFlow(tunedFeed as MorphoData<MorphoDataItem>)
                                else _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
                    val query = GetAuthorFeedQuery(id, limit, cur.cursor, GetAuthorFeedFilter.POSTS_WITH_REPLIES)
                    api.api.getAuthorFeed(query).onSuccess { response ->
                        if (response.cursor == cur.cursor) {
                            return@collect
                        }
                        var tunedFeed = MorphoData.concatFeed(
                            query = json.encodeToJsonElement(query),
                            responseCursor = response.cursor,
                            oldCursor = cur,
                            feed = response.feed,
                            data = (prev ?: MorphoData.EMPTY()) as MorphoData<MorphoDataItem.FeedItem>,
                            title = "Replies",
                        ).collectThreads(api = api).single()
                        useFeedTuners(tunedFeed).forEach { tuner ->
                            tunedFeed = tuner.tune(tunedFeed)
                        }
                        emit(Result.success(tunedFeed))
                        mutex.withLock {
                            if(prev == null) _dataFlows[uri] = MutableStateFlow(tunedFeed as MorphoData<MorphoDataItem>)
                            else _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
                cursor.collect { cur ->
                    val prev = dataFlows[uri]?.value
                    val query = GetAuthorFeedQuery(id, limit, cur.cursor, GetAuthorFeedFilter.POSTS_WITH_MEDIA)
                    api.api.getAuthorFeed(query).onSuccess { response ->
                        if (response.cursor == cur.cursor) {
                            return@collect
                        }
                        var tunedFeed = MorphoData.concatFeed(
                            query = json.encodeToJsonElement(query),
                            responseCursor = response.cursor,
                            oldCursor = cur,
                            feed = response.feed,
                            data = (prev ?: MorphoData.EMPTY()) as MorphoData<MorphoDataItem.FeedItem>,
                            title = "Media",
                        )
                        useFeedTuners(tunedFeed).forEach { tuner ->
                            tunedFeed = tuner.tune(tunedFeed)
                        }
                        emit(Result.success(tunedFeed))
                        mutex.withLock {
                            if(prev == null) _dataFlows[uri] = MutableStateFlow(tunedFeed as MorphoData<MorphoDataItem>)
                            else _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
                profileFeedsList(id, cursor, limit, dispatcher)
                    .collect { emit(it as Result<MorphoData<MorphoDataItem>>) }
            }
            FeedType.PROFILE_USER_LISTS -> {
                profileLists(id, cursor, limit, dispatcher)
                    .collect { emit(it as Result<MorphoData<MorphoDataItem>>) }
            }
            FeedType.PROFILE_LIKES -> {
                profileLikes(id, cursor, limit, dispatcher)
                    .collect { emit(it as Result<MorphoData<MorphoDataItem>>) }
            }
            FeedType.PROFILE_MOD_SERVICE -> {
                if (id.toString().startsWith("did:"))
                    profileServiceView(Did(id.toString()), cursor.map { Unit }.shareIn(scope, SharingStarted.Lazily), dispatcher)
                    .collect { emit(it as Result<MorphoData<MorphoDataItem>>) }
            }
            else -> {
                authorFeed(id, type, cursor, limit, dispatcher)
                    .collect { emit(it as Result<MorphoData<MorphoDataItem>>) }
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("${type.name} content for $id"))
    suspend fun profileLists(
        id: AtIdentifier,
        cursor: SharedFlow<AtCursor>,
        limit: Long = 50,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ): Flow<Result<MorphoData<MorphoDataItem.ListInfo>>> = flow<Result<MorphoData<MorphoDataItem.ListInfo>>> {
        val uri = AtUri.profileUserListsUri(id)
        cursor.collect { cur ->
            val prev = dataFlows[uri]?.value
            val query = GetListsQuery(id, limit, cur.cursor)
            api.api.getLists(query).onSuccess { response ->
                val data = if (cur != AtCursor.EMPTY && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(prev, response.lists.mapImmutable { MorphoDataItem.ListInfo(it.toList()) })
                } else if (cur == AtCursor.EMPTY && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(response.lists.mapImmutable { MorphoDataItem.ListInfo(it.toList()) }, prev)
                } else {
                    MorphoData("Lists", uri, AtCursor(response.cursor, cur.scroll),
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
    ): Flow<Result<MorphoData<MorphoDataItem.FeedInfo>>> = flow<Result<MorphoData<MorphoDataItem.FeedInfo>>> {
        val uri = AtUri.profileFeedsListUri(id)
        cursor.onEach { cur ->
            val prev = dataFlows[uri]?.value
            val query = GetActorFeedsQuery(id, limit, cur.cursor)
            api.api.getActorFeeds(query).onSuccess { response ->
                val data = if (cur != AtCursor.EMPTY && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(prev, response.feeds.mapImmutable { MorphoDataItem.FeedInfo(it.toFeedGenerator()) })
                } else if (cur == AtCursor.EMPTY && prev != null && prev.items.isNotEmpty()) {
                    MorphoData.concat(response.feeds.mapImmutable { MorphoDataItem.FeedInfo(it.toFeedGenerator()) }, prev)
                } else {
                    MorphoData("Feeds", uri, AtCursor(response.cursor, cur.scroll),
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
    ): Flow<Result<MorphoData<MorphoDataItem.LabelService>>> = flow<Result<MorphoData<MorphoDataItem.LabelService>>> {
        val uri = AtUri.profileModServiceUri(did)
        update.collect {
            val query = GetServicesQuery(listOf(did).toImmutableList(), true)
            api.api.getServices(query).onSuccess { response ->
                val data = MorphoData("Labels", uri, AtCursor.EMPTY,
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
    ): Flow<Result<MorphoData<MorphoDataItem.FeedItem>>> = flow<Result<MorphoData<MorphoDataItem.FeedItem>>> {
        val uri = AtUri.profileUserListsUri(id)
        cursor.collect { cur ->
            val prev = dataFlows[uri]?.value

            val query = GetActorLikesQuery(id, limit, cur.cursor)
            api.api.getActorLikes(query) .onSuccess { response ->
                var tunedFeed = MorphoData.concatFeed(
                    query = json.encodeToJsonElement(query),
                    responseCursor = response.cursor,
                    oldCursor = cur,
                    feed = response.feed,
                    data = (prev ?: MorphoData.EMPTY()) as MorphoData<MorphoDataItem.FeedItem>,
                    title = "Likes",
                )
                useFeedTuners(tunedFeed).forEach { tuner ->
                    tunedFeed = tuner.tune(tunedFeed)
                }
                emit(Result.success(tunedFeed))
                mutex.withLock {
                    if(prev == null) _dataFlows[uri] = MutableStateFlow(tunedFeed as MorphoData<MorphoDataItem>)
                    else _dataFlows[uri]?.update { tunedFeed as MorphoData<MorphoDataItem> }
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
        profiles: List<AtIdentifier>,
        update: SharedFlow<Unit>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Flow<Result<MorphoData<MorphoDataItem.ProfileItem>>> = flow<Result<MorphoData<MorphoDataItem.ProfileItem>>> {
        val uri = AtUri.myUserListUri(profiles.hashCode().toString())
        update.collect {
            val query = GetProfilesQuery(profiles.toPersistentList())
            api.api.getProfiles(query).onSuccess { response ->

                val data = MorphoData("Profiles", uri, AtCursor.EMPTY,
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
        update: SharedFlow<Unit> = MutableSharedFlow(),
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
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
                FeedType.LIST_FOLLOWING -> {
                    val query = GetListFeedQuery(feed.uri, 1)
                    api.api.getListFeed(query).onSuccess { response ->
                            if (response.feed.isNotEmpty()) {
                                val cid = response.feed.first().post.cid
                                if (!feed.contains(cid)) {
                                    emit(MorphoDataItem.Post(response.feed.first().toPost()))
                                } else {
                                    emit(null)
                                }
                            }
                        }.onFailure {
                            emit(null)
                        }
                }
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher)



    fun checkIfNewTimeline(
        interval: Long = 60000,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Flow<Boolean> = flow {
        val updateTick = UpdateTick(interval)
        updateTick.tick(true)
        updateTick.t.collect {
            val query = GetTimelineQuery(limit = 1, cursor = null)
            api.api.getTimeline(query).onSuccess { response ->
                if (response.feed.isNotEmpty()) {
                    val cid = response.feed.first().post.cid
                    if (dataFlows[AtUri.HOME_URI]?.value?.contains(cid) == false) {
                        emit(true)
                    } else {
                        emit(false)
                    }
                }
            }.onFailure { emit(false) }
        }
    }.distinctUntilChanged().flowOn(dispatcher)

    fun removeFeed(uri: AtUri): MorphoData<MorphoDataItem>?  {
        return _dataFlows.remove(uri)?.value
    }
}
