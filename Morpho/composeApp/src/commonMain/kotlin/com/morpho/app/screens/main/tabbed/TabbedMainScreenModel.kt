package com.morpho.app.screens.main.tabbed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastFirstOrNull
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.bluesky.MorphoDataFeed
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.bluesky.Profile
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.TabbedScreenState
import com.morpho.app.model.uistate.UiLoadingState
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.butterfly.AtUri
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import org.lighthousegames.logging.logging

class TabbedMainScreenModel : MainScreenModel() {
    var skylineUiState: TabbedScreenState by mutableStateOf(TabbedScreenState())
        private set

    private val tabs = mutableStateListOf<ContentCardMapEntry>()

    companion object {
        val log = logging()
    }


    fun uriForTab(index: Int): AtUri {
        return tabs[index].uri
    }

    @Suppress("UNCHECKED_CAST")
    // TODO: Revisit these casts if we can, but they should be safe
    // opting to return the potentially modified state even though it makes this a suspend function
    suspend fun <T: MorphoDataItem> addTab(tab: ContentCardMapEntry, state: ContentCardState<T>): ContentCardState<out MorphoDataItem> = screenModelScope.async {
        var newState: ContentCardState<out MorphoDataItem> = state
        when (state) {
            is ContentCardState.Skyline<*> -> {
                tabs.add(tab)
                if (state.feed.list.value.isEmpty()) {
                    loadFeed(state as ContentCardState.Skyline<MorphoDataItem.FeedItem>).await().onSuccess { newState = it }
                } else {
                    feedStates.add(state  as ContentCardState.Skyline<MorphoDataItem.FeedItem>)
                }
            }

            is ContentCardState.PostThread -> {
                history.push(tab)
                if (state.thread.value == null) {
                    loadThread(state).await().onSuccess {
                        threadStates.add(it)
                        newState = it// as ContentCardState<MorphoDataItem>
                    }

                } else {
                    threadStates.add(state)
                }
            }

            is ContentCardState.FullProfile<*> -> {
                history.push(tab)
                newState = loadProfile(state).await()
            }

            is ContentCardState.ProfileTimeline<*> -> {
                history.push(tab)
                newState = loadProfile(state).await() ?: return@async state
                if(newState is ContentCardState.FullProfile<*>) {
                    profileStates.add(newState as ContentCardState.FullProfile<Profile>)
                }
            }

            is ContentCardState.UserList -> TODO()
        }

        skylineUiState = skylineUiState.copy(
            tabs = tabs.toImmutableList(),
            tabStates = feedStates.toImmutableList()
        )
        return@async newState
    }.await()

    suspend fun switchTab(index: Int): Boolean = screenModelScope.async {
        if(index < 0 || index > tabs.lastIndex) return@async false
        val uri = tabs[index].uri
        val map = skylineUiState.tabMap.toMutableMap()
        val state = map[uri] ?: return@async false
        loadState(state).await().onSuccess {newState ->
            map[uri] = newState
            val list = map.values.toImmutableList()
            val i = list.indexOfFirst { it.uri == uri }
            if (i == -1) return@async false
            skylineUiState = skylineUiState.copy(selectedTabIndex = i, tabStates = list)
            return@async true
        }
        return@async false
    }.await()
    suspend fun switchTab(entry: ContentCardMapEntry): Boolean = screenModelScope.async {
        loadState(entry).await().onSuccess { newState ->
            val map = skylineUiState.tabMap.toMutableMap()
            map[entry.uri] = newState as ContentCardState<MorphoDataItem.FeedItem>
            val list = map.values.toImmutableList()
            val i = list.indexOfFirst { it.uri == entry.uri }
            if (i == -1) return@async false
            skylineUiState = skylineUiState.copy(selectedTabIndex = i, tabStates = list)
            return@async true
        }
        return@async false
    }.await()

    suspend fun switchTab(uri: AtUri): Boolean = screenModelScope.async {
        val tab = skylineUiState.tabs.fastFirstOrNull { it.uri == uri }
        return@async if (tab != null) switchTab(tab) else false
    }.await()

    suspend fun loadContent(state: ContentCardState<MorphoDataItem>) = screenModelScope.async {
        skylineUiState = skylineUiState.copy(loadingState = UiLoadingState.Loading)
        return@async loadState(state).await()
    }

    suspend fun loadTab(index: Int) = screenModelScope.async {
        if(index < 0 || index > tabs.lastIndex) return@async Result.failure(Exception("Tab index out of bounds"))
        val uri = tabs[index].uri
        val state = skylineUiState.tabMap[uri] ?: return@async Result.failure(Exception("Tab state not found"))
        skylineUiState = skylineUiState.copy(loadingState = UiLoadingState.Loading)
        return@async loadState(state).await()
    }

    override fun unloadContent(entry: ContentCardMapEntry): MorphoDataFeed? {
        val maybeTab = skylineUiState.tabMap[entry.uri]
        return if(maybeTab == null) {
            history.popUntil { it == entry }
            unloadContent(entry.uri)
        } else {
            unloadContent(maybeTab)
        }
    }

    fun unloadTab(index: Int): MorphoDataFeed? {
        if(index < 0 || index > tabs.lastIndex) return null
        val uri = tabs[index].uri
        val state = skylineUiState.tabMap[uri] ?: return null
        return unloadContent(state)
    }

}