package com.morpho.app.screens.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import app.bsky.actor.GetProfileQuery
import app.bsky.actor.SavedFeed
import app.bsky.feed.GetFeedGeneratorsQuery
import app.bsky.feed.GetPostThreadQuery
import app.bsky.feed.GetPostThreadResponseThreadUnion
import app.bsky.feed.GetPostsQuery
import cafe.adriel.voyager.core.stack.mutableStateStackOf
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uidata.*
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.ContentLoadingState
import com.morpho.app.model.uistate.FeedType
import com.morpho.app.screens.base.BaseScreenModel
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.inject
import org.lighthousegames.logging.logging


@Suppress("UNCHECKED_CAST")
// TODO: Revisit these casts if we can, but they should be safe
open class MainScreenModel: BaseScreenModel() {
    protected val dataService: BskyDataService by inject()



    protected val _feedStates = mutableListOf<StateFlow<ContentCardState.Skyline<MorphoDataItem.FeedItem>>>()
    val feedStates: List<StateFlow<ContentCardState.Skyline<MorphoDataItem.FeedItem>>>
        get() = _feedStates.toList()
    protected val _threadStates = mutableListOf<StateFlow<ContentCardState.PostThread>>()
    protected val _profileStates = mutableListOf<StateFlow<ContentCardState.FullProfile<Profile>>>()
    protected val _profileFeeds = mutableListOf<StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>>()


    val history = mutableStateStackOf<ContentCardMapEntry>()
    val settings: SettingsService by inject()

    protected val pinnedFeeds = MutableStateFlow(emptyList<UISavedFeed>())
    protected val savedFeeds = MutableStateFlow(emptyList<UISavedFeed>())

    protected val _cursors = mutableMapOf<AtUri, MutableSharedFlow<AtCursor>>()
    public  val cursors: ImmutableMap<AtUri, MutableSharedFlow<AtCursor>>
        get() = _cursors.toImmutableMap()

    var userId: AtIdentifier? by mutableStateOf(null)
        protected set

    var currentUser: DetailedProfile? by mutableStateOf(null)
        protected set

    protected var initialized = false
    companion object {
        val log = logging()
    }

    suspend fun init(populateFeeds: Boolean = true) = runBlocking {
        if(initialized) return@runBlocking
        initialized = true
        userId = api.atpUser?.id

        if(userId != null){
            if(preferences.prefs.firstOrNull().isNullOrEmpty()){
                val prefs = userId?.let {
                    preferences.getPreferences(it, true)
                }?.getOrNull()
                log.d { "Preferences: $prefs" }
                if(prefs != null) {
                    currentUser = settings.currentUser.value?.getProfile()
                } else {
                    log.e { "Failed to get preferences" }
                }
            } else if(preferences.getUser(userId!!).isFailure) {
                currentUser = userId?.let { GetProfileQuery(it) }?.let {
                    api.api.getProfile(it).getOrNull()?.toProfile()
                }
                val prefs = userId?.let {
                    api.api.getPreferences().getOrNull()?.toPreferences()
                }
                if(prefs != null && currentUser != null) {
                    preferences.setPreferences(BskyUser.makeUser(currentUser!!), prefs)
                } else {
                    log.e { "Failed to get preferences" }
                }
            } else {
                log.d { "Preferences already set maybe?" }
            }
            currentUser = settings.currentUser.value?.getProfile()

            if(currentUser == null) {
                currentUser = userId?.let { GetProfileQuery(it) }?.let {
                    api.api.getProfile(it).getOrNull()?.toProfile()
                }

            }
        }
        if(populateFeeds) {
            viewModelScope.launch(Dispatchers.Default) {
                settings.pinnedFeeds.collect { feeds ->
                    log.d { "Pinned Feeds: $feeds" }
                    pinnedFeeds.value = feeds
                }
                settings.savedFeeds.collect { feeds ->
                    log.d { "Saved Feeds: $feeds" }
                    savedFeeds.value = feeds
                }
            }
            initFeeds()
        }
    }

    fun getFeedInfo(uri: AtUri) : FeedInfo? {
        when {
            uri == AtUri.HOME_URI -> return FeedInfo(
                uri,
                "Home",
                "Your home feed",
                icon = Icons.Default.Home
            )

            else -> {
                if(savedFeeds.value.isNotEmpty()) {
                    savedFeeds.value.firstOrNull {
                        when (it.type) {
                            is UIFeedType.Feed -> it.type.uri == uri
                            is UIFeedType.List -> it.type.uri == uri
                            is UIFeedType.Timeline -> return FeedInfo(
                                uri,
                                "Home",
                                "Your home feed",
                                icon = Icons.Default.Home
                            )
                        }
                    }?.let {
                        if (it.type is UIFeedType.Feed) {
                            return FeedInfo(uri, it.title, it.description, it.avatar, feed = it.feed)
                        } else if (it.type is UIFeedType.List) {
                            return FeedInfo(uri, it.title, it.description, it.avatar, list = it.list)
                        }
                        // TODO: Get the feed info from the data service
                        return null
                    }
                }

            }
        }
        log.e { "Failed to get feed info for $uri" }
        return null
    }

    protected open suspend fun initFeeds() {
        val tlFlow = if(userId != null) {
            initTimeline(initAtCursor()).first()
        } else null

        if (tlFlow == null) {
            log.e { "Failed to initialize timeline" }
            // Init some default feeds
        }

        if (settings.pinnedFeeds.last().isNotEmpty()) {
            settings.pinnedFeeds.collectLatest { feeds ->
                feeds.forEach { feed ->
                    val flow =
                        initFeed(feed.feed!!, initAtCursor(), force = true, start = true).first()
                    if (flow == null) {
                        log.e { "Failed to initialize feed: ${feed.title}" }
                    }
                }
            }
        } else {
            // Init some default feeds
            api.api.getFeedGenerators(GetFeedGeneratorsQuery(
                persistentListOf(
                    AtUri("at://did:plc:z72i7hdynmk6r22z27h6tvur/app.bsky.feed.generator/whats-hot"),
                    AtUri("at://did:plc:tenurhgjptubkk5zf5qhi3og/app.bsky.feed.generator/discover"),
                    AtUri("at://did:plc:z72i7hdynmk6r22z27h6tvur/app.bsky.feed.generator/with-friends"),
                    AtUri("at://did:plc:tenurhgjptubkk5zf5qhi3og/app.bsky.feed.generator/feed-of-feeds"),
                )
            )).onSuccess { resp ->
                settings.addSavedFeeds(resp.feeds.map { feed ->
                    SavedFeed(
                        feed.uri.atUri,
                        pinned = true,
                        type = app.bsky.actor.FeedType.FEED,
                        value = feed.uri.atUri,
                    )
                })
                settings.savedFeeds.collectLatest { feeds ->
                    feeds.forEach { feed ->
                        val flow =
                            initFeed(feed.feed!!, initAtCursor(), force = true, start = false).first()
                        if (flow == null) {
                            log.e { "Failed to initialize feed: ${feed.title}" }
                        }
                    }
                }

            }
        }
    }

    fun updateFeed(uri: AtUri, newCursor: AtCursor = AtCursor.EMPTY): Boolean {
        val cursor = _cursors[uri] ?: return false
        if(newCursor.cursor == null && newCursor.scroll > 0) {
            val state = _feedStates.firstOrNull { it.value.uri == uri }
            val index = _feedStates.indexOfFirst { it.value.uri == uri }
            if(state != null) {
                val newState = state.value.copy(feed = state.value.feed.copy(cursor = newCursor))
                _feedStates[index] = MutableStateFlow(newState).asStateFlow()
                return true
            }
            val profileFeedState = _profileFeeds.firstOrNull { it.value.uri == uri }
            val profileFeedIndex = _profileFeeds.indexOfFirst { it.value.uri == uri }
            if(profileFeedState != null) {
                val newState = profileFeedState.value.copy(feed = profileFeedState.value.feed.copy(cursor = newCursor))
                _profileFeeds[profileFeedIndex] = MutableStateFlow(newState).asStateFlow()
                return true
            }
        }
        return cursor.tryEmit(newCursor)
    }

    fun updateFeed(feed: FeedGenerator, newCursor: AtCursor = AtCursor.EMPTY): Boolean {
        return updateFeed(feed.uri, newCursor)
    }

    fun updateFeed(entry: ContentCardMapEntry, newCursor: AtCursor = AtCursor.EMPTY): Boolean {
        return updateFeed(entry.uri, newCursor)
    }

    open suspend fun peekLatest(entry: ContentCardMapEntry, update: SharedFlow<Unit>? = null): StateFlow<MorphoDataItem?> = flow {
        val feed =
            _feedStates.firstOrNull { it.value.uri == entry.uri }
                ?: _profileFeeds.firstOrNull { it.value.uri == entry.uri }
                ?: _profileStates.firstOrNull { it.value.uri == entry.uri }
                ?: _threadStates.firstOrNull { it.value.uri == entry.uri }
        if(feed == null) { emit(null); return@flow }
        if(update == null) dataService.peekLatest(feed.value.feed).onEach { emit(it) }
        else dataService.peekLatest(feed.value.feed, update).onEach { emit(it) }
    }.stateIn(viewModelScope)

    suspend fun loadThread(uri: AtUri): StateFlow<ContentCardState.PostThread>? {
        val state = _threadStates.firstOrNull { it.value.uri == uri }
        if(state != null) return state
        val post =
            api.api.getPosts(GetPostsQuery(persistentListOf(uri))).map { it.posts.firstOrNull()?.toPost() }.getOrNull()
                ?: return null
        return loadThread(ContentCardState.PostThread(post, MutableStateFlow(null).asStateFlow(), ContentLoadingState.Loading))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun loadThread(state: ContentCardState.PostThread): StateFlow<ContentCardState.PostThread> = flow {
        val r = api.api.getPostThread(GetPostThreadQuery(state.uri, 15, 200)).map { response ->
            response.thread.let { thread ->
                when (thread) {
                    is GetPostThreadResponseThreadUnion.BlockedPost -> {
                        ContentCardState.PostThread(
                            state.post,
                            MutableStateFlow(null).asStateFlow(),
                            ContentLoadingState.Error("Blocked post")
                        )
                    }

                    is GetPostThreadResponseThreadUnion.NotFoundPost -> {
                        ContentCardState.PostThread(
                            state.post,
                            MutableStateFlow(null).asStateFlow(),
                            ContentLoadingState.Error("Post not found")
                        )
                    }

                    is GetPostThreadResponseThreadUnion.ThreadViewPost -> {
                        ContentCardState.PostThread(
                            thread.value.toPost(),
                            MutableStateFlow(thread.value.toThread()).asStateFlow(),
                            ContentLoadingState.Idle
                        )
                    }
                }
            }
        }
        emit(r.getOrDefault(state.copy(loadingState = ContentLoadingState.Error("Failed to load thread"))))
    }.stateIn(viewModelScope)

    private fun <T: MorphoDataItem> indexOf(state: ContentCardState<T>): Int {
        return when(state) {
            is ContentCardState.FullProfile<*> -> _profileStates.indexOfFirst { it.value.uri == state.uri }
            is ContentCardState.PostThread      -> _threadStates.indexOfFirst { it.value.uri == state.uri }
            is ContentCardState.ProfileTimeline<T> -> _profileFeeds.indexOfFirst { it.value.uri == state.uri }
            is ContentCardState.Skyline<*> -> _feedStates.indexOfFirst { it.value.uri == state.uri }
            is ContentCardState.UserList -> TODO()
        }
    }

    suspend fun initFeed(
        feed: ContentCardMapEntry.Feed,
        force: Boolean = false,
        start: Boolean = true,
        limit: Long = 100,
    ): Flow<ContentCardState.Skyline<MorphoDataItem.FeedItem>?> = flow {
        val info = getFeedInfo(feed.uri)
        if(info == null) { emit(null); return@flow }
        val feedService = dataService.dataFlows[feed.uri]

        // Delete the feed if it's already there, initializing from scratch
        if(force && feedService != null) dataService.removeFeed(feed.uri)
        _cursors[feed.uri] = feed.cursorFlow
        if(start) feed.cursorFlow.emit(AtCursor.EMPTY)

        val feedState = _feedStates
            .firstOrNull { it.value.uri == feed.uri }
        val newFeed = dataService
            .feed(info, feed.cursorFlow, limit)
            .handleToState(MorphoData(info.name, feed.uri, feed.cursorFlow.replayCache.lastOrNull() ?: AtCursor.EMPTY))

        if (feedState == null) {
            _feedStates.add(newFeed)
            emit(newFeed.value)

        } else {
            val i = _feedStates.indexOf(feedState)
            _feedStates[i] = newFeed.filterNotNull().stateIn(viewModelScope)
            emit(newFeed.value)
        }
    }

    suspend fun initFeed(
        feed: FeedGenerator,
        cursor: MutableSharedFlow<AtCursor>,
        force: Boolean = false,
        start: Boolean = true,
        limit: Long = 100,
    ): Flow<ContentCardState.Skyline<MorphoDataItem.FeedItem>?> = flow {
        val feedService = dataService.dataFlows[feed.uri]
        val info = FeedInfo(feed.uri, feed.displayName, feed.description, feed.avatar, feed = feed)
        // Delete the feed if it's already there, initializing from scratch
        if(force && feedService != null) dataService.removeFeed(feed.uri)
        _cursors[feed.uri] = cursor
        if(start) cursor.emit(AtCursor.EMPTY)

        val feedState = _feedStates
            .firstOrNull { it.value.uri == feed.uri }
        val newFeed = dataService
            .feed(info, cursor, limit)
            .handleToState(MorphoData(feed.displayName, feed.uri, cursor.replayCache.lastOrNull() ?: AtCursor.EMPTY))

        if (feedState == null) {
            _feedStates.add(newFeed)
            emit(newFeed.value)

        } else {
            val i = _feedStates.indexOf(feedState)
            _feedStates[i] = newFeed
            emit(newFeed.value)
        }
    }


    suspend fun initTimeline(
        timeline: ContentCardMapEntry.Home,
        force: Boolean = false,
    ): Flow<ContentCardState.Skyline<MorphoDataItem.FeedItem>?> = flow {
        if(timeline.uri != AtUri.HOME_URI) { emit(null); return@flow }
        val prefs = if(preferences.prefs.firstOrNull().isNullOrEmpty()) {
            log.d { "No preferences found"}
            MutableStateFlow(BskyFeedPref())
        } else {
            log.d { "Preferences found"}
            settings.feedViewPrefs.map {
                it["home"] ?: BskyFeedPref()
            }.stateIn(viewModelScope, SharingStarted.Lazily, BskyFeedPref())
        }
        val feedService = dataService.dataFlows[timeline.uri]
        log.d { "Timeline service: $feedService"}
        // Delete the feed if it's already there, initializing from scratch
        if(force && feedService != null) dataService.removeFeed(timeline.uri)
        _cursors[timeline.uri] = timeline.cursorFlow
        timeline.cursorFlow.emit(AtCursor.EMPTY)
        val feedState = _feedStates
            .firstOrNull { it.value.uri == timeline.uri }
        log.d { "Timeline state: $feedState"}
        val newFeed = dataService
            .timeline(timeline.cursorFlow, 100, prefs)
            .handleToState(MorphoData(cursor = timeline.cursorFlow.replayCache.lastOrNull() ?: AtCursor.EMPTY))

        if (feedState == null) {
            _feedStates.add(newFeed)
            emit(newFeed.value)
        } else {
            val i = _feedStates.indexOf(feedState)
            _feedStates[i] = newFeed
            emit(newFeed.value)
        }
    }

    suspend fun initTimeline(
        cursor: MutableSharedFlow<AtCursor>,
        force: Boolean = false,
    ): Flow<ContentCardState.Skyline<MorphoDataItem.FeedItem>?> = flow {
        val uri = AtUri.HOME_URI
        val prefs = settings.feedViewPrefs.map {
            it["home"] ?: BskyFeedPref()
        }.filterNotNull().stateIn(viewModelScope)
        val feedService = dataService.dataFlows[uri]

        // Delete the feed if it's already there, initializing from scratch
        if(force && feedService != null) dataService.removeFeed(uri)
        _cursors[uri] = cursor
        cursor.emit(AtCursor.EMPTY)
        val feedState = _feedStates
            .firstOrNull { it.value.uri == uri }
        val newFeed = dataService
            .timeline(cursor, 100, prefs)
            .handleToState(MorphoData(cursor = cursor.replayCache.lastOrNull() ?: AtCursor.EMPTY))

        if (feedState == null) {
            _feedStates.add(newFeed)
            emit(newFeed.value)
        } else {
            val i = _feedStates.indexOf(feedState)
            _feedStates[i] = newFeed
            emit(newFeed.value)
        }
    }

    suspend fun initProfileTabContent(
        feed: ContentCardMapEntry,
        force: Boolean = false,
        limit: Long = 100,
    ): Flow<ContentCardState.ProfileTimeline<MorphoDataItem>?> = flow {
        // Has to be a profile feed
        if(!feed.uri.isProfileFeed) { emit(null); return@flow }
        val id = feed.uri.id(api)
        val feedService = dataService.dataFlows[feed.uri]

        // Delete the feed if it's already there, initializing from scratch
        if(force && feedService != null) dataService.removeFeed(feed.uri)
        _cursors[feed.uri] = feed.cursorFlow

        val feedState =
            if(_profileStates.firstOrNull { it.value.profile.did == id } != null) {
                _profileStates.firstOrNull { it.value.profile.did == id }
        } else _profileFeeds.firstOrNull { it.value.uri == feed.uri }
        val profile = if(_profileStates.firstOrNull{ it.value.profile.did == id } != null) {
                _profileStates.firstOrNull{ it.value.profile.did == id }?.value?.profile
            } else api.api.getProfile(GetProfileQuery(id)).getOrNull()?.toProfile()
        if (profile == null) { emit(null); return@flow }
        val newFeed = dataService
            .profileTabContent(id, feed.feedType, feed.cursorFlow, limit)
            .handleToState(profile, MorphoData(feed.title, feed.uri, feed.cursorFlow.replayCache.lastOrNull() ?: AtCursor.EMPTY))
        if (feedState == null) {
            _profileFeeds.add(newFeed)
            emit(_profileFeeds.last().value)
        } else {
            val i = _profileFeeds.indexOf(feedState)
            _profileFeeds[i] = newFeed
            emit(_profileFeeds[i].value)
        }
        feed.cursorFlow.emit(AtCursor.EMPTY)
    }

    suspend fun initProfileContent(
        profile: ContentCardMapEntry.Profile,
        force: Boolean = false,
        fill: Boolean = false,
    ): Flow<ContentCardState.FullProfile<Profile>?> = flow {
        val feedService = dataService.dataFlows[profile.uri]
        // Delete the feed if it's already there, initializing from scratch
        if(force && feedService != null) dataService.removeFeed(profile.uri)
        val feedState = _profileStates.firstOrNull { it.value.profile.did == profile.uri.id(api) }
        val p = if(_profileStates.firstOrNull{ it.value.profile.did == profile.id } != null) {
            _profileStates.firstOrNull{ it.value.profile.did == profile.id }?.value?.profile
        } else api.api.getProfile(GetProfileQuery(profile.id)).getOrNull()?.toProfile()
        if (p == null) { emit(null); return@flow }
        val newProfile = if(fill) {

            val postsCursor: MutableSharedFlow<AtCursor> = initAtCursor()
            _cursors[AtUri.profilePostsUri(p.did)] = postsCursor
            val posts = dataService
                .authorFeed(p.did, FeedType.PROFILE_POSTS, postsCursor.asSharedFlow(), 50)
                .handleToState(p, MorphoData("Posts", AtUri.profilePostsUri(p.did),
                                             postsCursor.replayCache.lastOrNull() ?: AtCursor.EMPTY))

            val repliesCursor: MutableSharedFlow<AtCursor> = initAtCursor()
            _cursors[AtUri.profileRepliesUri(p.did)] = repliesCursor
            val replies = dataService
                .authorFeed(p.did, FeedType.PROFILE_REPLIES, repliesCursor.asSharedFlow(), 50)
                .handleToState(p, MorphoData("Replies", AtUri.profileRepliesUri(p.did),
                                             repliesCursor.replayCache.lastOrNull() ?: AtCursor.EMPTY))

            val mediaCursor: MutableSharedFlow<AtCursor> = initAtCursor()
            _cursors[AtUri.profileMediaUri(p.did)] = mediaCursor
            val media = dataService
                .authorFeed(p.did, FeedType.PROFILE_MEDIA, mediaCursor.asSharedFlow(), 50)
                .handleToState(p, MorphoData("Media", AtUri.profileMediaUri(p.did),
                                             mediaCursor.replayCache.lastOrNull() ?: AtCursor.EMPTY))

            val likesCursor: MutableSharedFlow<AtCursor> = initAtCursor()
            _cursors[AtUri.profileLikesUri(p.did)] = likesCursor
            val likes = dataService
                .profileLikes(p.did, likesCursor.asSharedFlow(), 50)
                .handleToState(p, MorphoData("Likes", AtUri.profileLikesUri(p.did),
                                             likesCursor.replayCache.lastOrNull() ?: AtCursor.EMPTY))

            val listsCursor: MutableSharedFlow<AtCursor> = initAtCursor()
            _cursors[AtUri.profileUserListsUri(p.did)] = listsCursor
            val lists = dataService
                .profileLists(p.did, listsCursor.asSharedFlow(), 50)
                .handleToState(p, MorphoData("Lists", AtUri.profileUserListsUri(p.did),
                                             listsCursor.replayCache.lastOrNull() ?: AtCursor.EMPTY))

            val feedsCursor: MutableSharedFlow<AtCursor> = initAtCursor()
            _cursors[AtUri.profileFeedsListUri(p.did)] = feedsCursor
            val feeds = dataService
                .profileFeedsList(p.did, feedsCursor.asSharedFlow(), 50)
                .handleToState(p, MorphoData("Feeds", AtUri.profileFeedsListUri(p.did),
                                             feedsCursor.replayCache.lastOrNull() ?: AtCursor.EMPTY))

            if (p is BskyLabelService) {
                val servicesCursor: MutableSharedFlow<AtCursor> = initAtCursor()
                _cursors[AtUri.profileModServiceUri(p.did)] = servicesCursor
                val services = dataService
                    .profileServiceView(p.did, servicesCursor.map { Unit }
                        .shareIn(viewModelScope, SharingStarted.Lazily)
                    ).handleToState(p, MorphoData("Labels", AtUri.profileModServiceUri(p.did),
                                                  servicesCursor.replayCache.lastOrNull() ?: AtCursor.EMPTY))
                servicesCursor.emit(AtCursor.EMPTY)
                ContentCardState.FullProfile(
                    p,
                    posts.stateIn(viewModelScope),
                    replies.stateIn(viewModelScope),
                    media.stateIn(viewModelScope),
                    likes.stateIn(viewModelScope),
                    lists.stateIn(viewModelScope),
                    feeds.stateIn(viewModelScope),
                    services.stateIn(viewModelScope),
                    ContentLoadingState.Idle
                )
            } else {
                postsCursor.emit(AtCursor.EMPTY)
                ContentCardState.FullProfile(
                    p,
                    posts.stateIn(viewModelScope),
                    replies.stateIn(viewModelScope),
                    media.stateIn(viewModelScope),
                    likes.stateIn(viewModelScope),
                    lists.stateIn(viewModelScope),
                    feeds.stateIn(viewModelScope),
                    loadingState = ContentLoadingState.Idle
                )
            }

        } else {
             ContentCardState.FullProfile(p, loadingState = ContentLoadingState.Loading)
        }
        if (feedState == null) {
            _profileStates.add(MutableStateFlow(newProfile).asStateFlow() as StateFlow<ContentCardState.FullProfile<Profile>>)
            emit(_profileStates.last().value)
        } else {
            val i = _profileStates.indexOf(feedState)
            _profileStates[i] = MutableStateFlow(newProfile).asStateFlow() as StateFlow<ContentCardState.FullProfile<Profile>>
            emit(_profileStates[i].value)
        }
    }

    fun <T: MorphoDataItem> unloadContent(state: ContentCardState<T>): MorphoData<T>? {

        when(state) {
            is ContentCardState.Skyline<*> -> {
                _feedStates.removeAll { it.value.uri == state.uri }
            }
            is ContentCardState.PostThread -> {
                _threadStates.removeAll { it.value.uri == state.uri }
            }
            is ContentCardState.FullProfile<*> -> {
                _profileStates.removeAll { it.value.uri == state.uri }
                unloadContent(state.postsState.value as ContentCardState<T>)
                unloadContent(state.postRepliesState.value as ContentCardState<T>)
                unloadContent(state.mediaState.value as ContentCardState<T>)
                unloadContent(state.likesState.value as ContentCardState<T>)
            }
            is ContentCardState.ProfileTimeline<T> -> {
                _profileFeeds.removeAll { it.value.uri == state.uri }
            }

            is ContentCardState.UserList -> TODO()
        }
        return dataService.removeFeed(state.uri) as MorphoData<T>?
    }

    protected open fun unloadContent(entry: ContentCardMapEntry): MorphoData<MorphoDataItem>? {
        return unloadContent(entry.uri)
    }

    protected fun unloadContent(uri: AtUri): MorphoData<MorphoDataItem>? {
        val state = _feedStates.firstOrNull { it.value.uri == uri }
            ?: _threadStates.firstOrNull { it.value.uri == uri }
            ?: _profileStates.firstOrNull { it.value.uri == uri }
            ?: _profileFeeds.firstOrNull { it.value.uri == uri }
            ?: return null
        return unloadContent(state.value) as MorphoData<MorphoDataItem>?
    }
}