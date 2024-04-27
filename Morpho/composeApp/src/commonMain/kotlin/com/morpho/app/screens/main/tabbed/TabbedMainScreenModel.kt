package com.morpho.app.screens.main.tabbed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.bsky.feed.GetFeedGeneratorsQuery
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.bluesky.FeedGenerator
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.bluesky.Profile
import com.morpho.app.model.bluesky.toFeedGenerator
import com.morpho.app.model.uidata.AtCursor
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uidata.MorphoData
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.TabbedScreenState
import com.morpho.app.model.uistate.UiLoadingState
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.butterfly.AtUri
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

class
TabbedMainScreenModel : MainScreenModel() {


    var uiState: TabbedScreenState by mutableStateOf(TabbedScreenState(loadingState = UiLoadingState.Loading))
        private set

    private val tabs = mutableListOf<ContentCardMapEntry>()

    val _tabFlow = MutableStateFlow(tabs.toImmutableList())
    val tabFlow: StateFlow<ImmutableList<ContentCardMapEntry>>
        get() = _tabFlow.asStateFlow()
    companion object {
        val log = logging()
    }

    fun uriForTab(index: Int): AtUri {
        return tabs[index].uri
    }

    fun initTabs() = screenModelScope.launch {
        if (initialized) return@launch
        init(false)
        initialized = true
        val home = initHomeTab()
        val savedFeedsPref = userPrefs.value?.preferences?.savedFeeds
        if (savedFeedsPref != null) {
            val newFeeds = mutableListOf<StateFlow<ContentCardState<MorphoDataItem>>>()

            tabs.clear()
            if(home.isSuccess) {
                val homeState = _feedStates.firstOrNull {
                    it.value.uri == home.getOrThrow().first.uri
                }
                if (homeState != null && home.getOrNull()?.second != null) {
                    tabs.add(home.getOrThrow().first)
                    _tabFlow.value = tabs.toImmutableList()
                    newFeeds.add(homeState as StateFlow<ContentCardState<MorphoDataItem>>)
                    //uiState = uiState.copy(loadingState = UiLoadingState.Idle, tabs = tabFlow, tabStates = newFeeds.toImmutableList())
                } else {
                    log.e { "Failed to initialize home tab state" }
                    log.d {
                        "Home tab: ${home.getOrNull()?.first}\n" +
                        "Home state: ${homeState?.value}"
                    }
                }
            }

            //uiState = uiState.copy(loadingState = UiLoadingState.Loading)
            val feeds = api.api
                .getFeedGenerators(GetFeedGeneratorsQuery(savedFeedsPref.pinned))
                .map { resp ->
                    _pinnedFeeds.addAll(resp.feeds.map{ it.toFeedGenerator() })
                    _pinnedFeeds.associateBy { _pinnedFeeds.indexOf(it) }.mapValues { feedGen ->
                        initFeedTab(feedGen.value)
                    }
                }.getOrNull()
            if (feeds != null) {
                feeds.forEach { (index, pair) ->
                    val feed = pair.getOrNull()
                    if (feed != null) {
                        feedStates.firstOrNull {
                            it.value.uri == feed.first.uri
                        }?.let { state ->
                            tabs.add(feed.first)
                            newFeeds.add(state as StateFlow<ContentCardState<MorphoDataItem>>)
                        }
                    } else {
                        log.e { "Failed to initialize feed tab at index $index" }
                    }
                }
                _tabFlow.value = tabs.toImmutableList()
                //uiState = uiState.copy(loadingState = UiLoadingState.Idle, tabs = tabFlow, tabStates = newFeeds.toImmutableList())
            }
            uiState = uiState.copy(loadingState = UiLoadingState.Idle, tabs = tabFlow, tabStates = newFeeds.toImmutableList())
        } else {
            // Init some default feeds
        }
        return@launch
    }

    fun refreshTab(index: Int, cursor: AtCursor = null) :Boolean {
        return if(index < 0 || index > tabs.lastIndex) false
        else updateFeed(tabs[index], cursor)
    }


    suspend fun initHomeTab():
            Result<Pair<ContentCardMapEntry.Home, ContentCardState.Skyline<MorphoDataItem.FeedItem>>> {
        val home = ContentCardMapEntry.Home
        _cursors[home.uri] = home.cursorFlow
        val f = initTimeline(home, force = false).first()

        return if(f != null) {
            Result.success(Pair(home, f))
        }  else {
            val ul = unloadContent(home)
            log.e { "Failed to initialize home tab" }
            log.v { "Deleted Feed: ${ul?.items}" }
            Result.failure(Exception("Failed to initialize home tab"))
        }
    }

    suspend fun initFeedTab(
        feed:FeedGenerator
    ): Result<Pair<ContentCardMapEntry.Feed, ContentCardState.Skyline<MorphoDataItem.FeedItem>>> {
        val title = feed.displayName
        val tab = ContentCardMapEntry.Feed(feed.uri, title, avatar = feed.avatar)
        _cursors[tab.uri] = tab.cursorFlow
        val f = initFeed(feed, tab.cursorFlow, force = true, start = false, limit = 50).firstOrNull()
        return if(f != null) {
            Result.success(Pair(tab, f))
        } else {
            val ul = unloadContent(tab)
            log.e { "Failed to initialize feed tab $title" }
            log.v { "Deleted Feed: ${ul?.items}" }
            Result.failure(Exception("Failed to initialize feed tab $title"))
        }
    }

    suspend fun initProfileTab(profile: Profile): Result<Pair<ContentCardMapEntry.Profile, ContentCardState.FullProfile<Profile>>> {
        val title = profile.displayName ?: profile.handle.handle
        val tab = ContentCardMapEntry.Profile(profile.did, AtUri.profileUri(profile.did), title)
        _cursors[tab.uri] = tab.cursorFlow
        val f = initProfileContent(tab, force = true, fill = true).first()
        return if(f != null) {
            Result.success(Pair(tab, f))
        } else {
            val ul = unloadContent(tab)
            log.e { "Failed to initialize profile tab $title" }
            log.v { "Deleted Feed: ${ul?.items}" }
            Result.failure(Exception("Failed to initialize profile tab $title"))
        }
    }



    /*@OptIn(ExperimentalCoroutinesApi::class)
    suspend fun switchTab(index: Int): ReceiveChannel<Boolean> = screenModelScope.produce{
        if(index < 0 || index > tabs.lastIndex) { send(false); return@produce }
        val uri = tabs[index].uri
        val map = skylineUiState.tabMap.toMutableMap()
        val state = map[uri]
        if (state == null) { send(false); return@produce }
        loadState(state).receive().onSuccess {newState ->
            map[uri] = newState as ContentCardState<MorphoDataItem.FeedItem>
            val list = map.values.toImmutableList()
            val i = list.indexOfFirst { it.uri == uri }
            if (i == -1) { send(false); return@produce }
            skylineUiState = skylineUiState.copy(selectedTabIndex = i, tabStates = list)
            send(true)
            return@produce
        }
        send(true)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun switchTab(entry: ContentCardMapEntry): ReceiveChannel<Boolean> = screenModelScope.produce {
        loadState(entry).receive().onSuccess { newState ->
            val map = skylineUiState.tabMap.toMutableMap()
            map[entry.uri] = newState as ContentCardState<MorphoDataItem.FeedItem>
            val list = map.values.toImmutableList()
            val i = list.indexOfFirst { it.uri == entry.uri }
            if (i == -1) { send(false); return@produce }
            skylineUiState = skylineUiState.copy(selectedTabIndex = i, tabStates = list)
            send(true)
            return@produce
        }
        send(true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun switchTab(uri: AtUri):  ReceiveChannel<Boolean> = screenModelScope.produce {
        val tab = skylineUiState.tabs.fastFirstOrNull { it.uri == uri }
        if (tab != null) send(switchTab(tab).receive()) else send(false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun loadContent(state: ContentCardState<MorphoDataItem>) = screenModelScope.produce {
        skylineUiState = skylineUiState.copy(loadingState = UiLoadingState.Loading)
        send(loadState(state).receive())
    }

    suspend fun loadTab(index: Int) = screenModelScope.produce {
        if(index < 0 || index > tabs.lastIndex) {
            send(Result.failure(Exception("Tab index out of bounds")))
            return@produce
        }
        val uri = tabs[index].uri
        val state = skylineUiState.tabMap[uri]
        if(state == null) {
            send(Result.failure(Exception("Tab state not found")))
            return@produce
        }
        skylineUiState = skylineUiState.copy(loadingState = UiLoadingState.Loading)
        send(loadState(state).receive())
    }*/

    override fun unloadContent(entry: ContentCardMapEntry): MorphoData<MorphoDataItem>? {
        val maybeTab = uiState.tabMap[entry.uri]
        return if(maybeTab == null) {
            history.popUntil { it == entry }
            unloadContent(entry.uri)
        } else {
            unloadContent(maybeTab)
        }
    }

    fun unloadTab(index: Int): MorphoData<MorphoDataItem>? {
        if(index < 0 || index > tabs.lastIndex) return null
        val uri = tabs[index].uri
        val state = uiState.tabMap[uri] ?: return null
        return unloadContent(state)
    }

}