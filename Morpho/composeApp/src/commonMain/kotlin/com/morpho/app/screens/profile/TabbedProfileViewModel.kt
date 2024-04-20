package com.morpho.app.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastFirstOrNull
import app.bsky.actor.GetProfileQuery
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.ContentLoadingState
import com.morpho.app.model.uistate.TabbedProfileScreenState
import com.morpho.app.model.uistate.UiLoadingState
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.auth.AtpUser
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

@Suppress("UNCHECKED_CAST")
// TODO: Revisit these casts if we can, but they should be safe
class TabbedProfileViewModel(
    val user: AtpUser? = null
): MainScreenModel() {

    companion object {
        val log = logging()
    }
    var profileUiState: TabbedProfileScreenState by mutableStateOf(TabbedProfileScreenState())
        private set

    var profileState: ContentCardState.FullProfile<Profile>? by mutableStateOf(null)
        private set

    private val tabs = mutableStateListOf<ContentCardMapEntry>()

    var userID: AtIdentifier?  by mutableStateOf(null)
        private set

    var myProfile: Boolean = false
        private set

    init {
        if(user != null) {
            userID = user.id
            myProfile = api.atpUser?.id == user.id
        } else {
            userID = api.atpUser?.id
            myProfile = true
        }
        log.d { "User ID: $userID"}
        initProfile()
    }



    fun initProfile() = screenModelScope.launch {
        if(user != null) {
            profileUiState = profileUiState.copy(loadingState = UiLoadingState.Loading)
            api.api.getProfile(GetProfileQuery(user.id))
                .onSuccess {
                    loadProfile(it.toProfile())
                    log.d { "Profile loaded: ${it.toProfile()}" }
                }.onFailure {
                    profileUiState = profileUiState.copy(loadingState = UiLoadingState.Error("Profile not loaded"))
                    log.e(it) { "Profile not loaded. Error: $it" }
                }
        }
    }

    fun loadProfile(profile: DetailedProfile) = screenModelScope.launch {
        profileState = ContentCardState.FullProfile(profile, loadingState = ContentLoadingState.Loading)
        profileState = loadProfile(profileState!!).await()
        if (!updateProfile()) {
            profileUiState = profileUiState
                .copy(loadingState = UiLoadingState.Error("Profile feeds not loaded"))
            log.e { "$profileUiState" }
        } else log.d { "Profile loaded: $profileState" }

    }

    fun updateProfile(): Boolean {
        if(user != null && profileState != null) {
            val tabs = mutableListOf<ContentCardMapEntry>()
            val tabStates =
                mutableListOf<ContentCardState.ProfileTimeline<MorphoDataItem>>()
            if (profileState?.modServiceState != null) {
                tabs.add(ContentCardMapEntry.ServiceList(profileState!!.modServiceState!!.uri, "Labels"))
                tabStates.add(profileState!!.modServiceState!! as ContentCardState.ProfileTimeline<MorphoDataItem>)
            }
            if ( profileState?.modServiceState != null && profileState?.listsState != null) {
                tabs.add(ContentCardMapEntry.UserList(profileState!!.listsState!!.uri, "Lists"))
                tabStates.add(profileState!!.listsState!! as ContentCardState.ProfileTimeline<MorphoDataItem>)
            }
            if (profileState?.postsState != null) {
                tabs.add(ContentCardMapEntry.Skyline(profileState!!.postsState!!.uri, "Posts"))
                tabStates.add(profileState!!.postsState!! as ContentCardState.ProfileTimeline<MorphoDataItem>)
            }
            if (profileState?.postRepliesState != null) {
                tabs.add(ContentCardMapEntry.Skyline(profileState!!.postRepliesState!!.uri, "Replies"))
                tabStates.add(profileState!!.postRepliesState!! as ContentCardState.ProfileTimeline<MorphoDataItem>)
            }
            if (profileState?.mediaState != null) {
                tabs.add(ContentCardMapEntry.Skyline(profileState!!.mediaState!!.uri, "Media"))
                tabStates.add(profileState!!.mediaState!! as ContentCardState.ProfileTimeline<MorphoDataItem>)
            }
            if (profileState?.likesState != null) {
                tabs.add(ContentCardMapEntry.Skyline(profileState!!.likesState!!.uri, "Likes"))
                tabStates.add(profileState!!.likesState!! as ContentCardState.ProfileTimeline<MorphoDataItem>)
            }
            if (profileState?.feedsState != null) {
                tabs.add(ContentCardMapEntry.FeedList(profileState!!.feedsState!!.uri, "Feeds"))
                tabStates.add(profileState!!.feedsState!! as ContentCardState.ProfileTimeline<MorphoDataItem>)
            }
            if ( profileState?.modServiceState == null && profileState?.listsState != null) {
                tabs.add(ContentCardMapEntry.UserList(profileState!!.listsState!!.uri, "Lists"))
                tabStates.add(profileState!!.listsState!! as ContentCardState.ProfileTimeline<MorphoDataItem>)
            }
            profileUiState = profileUiState.copy(
                loadingState = UiLoadingState.Idle,
                tabs = tabs.toImmutableList(),
                tabStates = tabStates.toImmutableList()
            )
            return true
        } else return false
    }

    suspend fun switchTab(index: Int): Boolean = screenModelScope.async {
        if(index < 0 || index > tabs.lastIndex) return@async false
        val uri = tabs[index].uri
        val map = profileUiState.tabMap.toMutableMap()
        val state = map[uri] ?: return@async false
        loadState(state).await().onSuccess {newState ->
            map[uri] = newState as ContentCardState.ProfileTimeline<MorphoDataItem>
            val list = map.values.toImmutableList()
            val i = list.indexOfFirst { it.uri == uri }
            if (i == -1) return@async false
            profileUiState = profileUiState.copy(selectedTabIndex = i, tabStates = list)
            return@async true
        }
        return@async false
    }.await()
    suspend fun switchTab(entry: ContentCardMapEntry): Boolean = screenModelScope.async {
        loadState(entry).await().onSuccess { newState ->
            val map = profileUiState.tabMap.toMutableMap()
            map[entry.uri] = newState as ContentCardState.ProfileTimeline<MorphoDataItem>
            val list = map.values.toImmutableList()
            val i = list.indexOfFirst { it.uri == entry.uri }
            if (i == -1) return@async false
            profileUiState = profileUiState.copy(selectedTabIndex = i, tabStates = list)
            return@async true
        }
        return@async false
    }.await()

    suspend fun switchTab(uri: AtUri): Boolean = screenModelScope.async {
        val tab = profileUiState.tabs.fastFirstOrNull { it.uri == uri }
        return@async if (tab != null) switchTab(tab) else false
    }.await()

    suspend fun loadContent(state: ContentCardState<MorphoDataItem>) = screenModelScope.async {
        profileUiState = profileUiState.copy(loadingState = UiLoadingState.Loading)
        return@async loadState(state).await()
    }

    suspend fun loadTab(index: Int) = screenModelScope.async {
        if(index < 0 || index > tabs.lastIndex) return@async Result.failure(Exception("Tab index out of bounds"))
        val uri = tabs[index].uri
        val map = profileUiState.tabMap.toPersistentMap()
        val state = map[uri] ?: return@async Result.failure(Exception("Tab state not found"))
        profileUiState = profileUiState.copy(loadingState = UiLoadingState.Loading)
        return@async loadState(state).await()
    }

    override fun unloadContent(entry: ContentCardMapEntry): MorphoDataFeed? {
        val maybeTab = profileUiState.tabMap[entry.uri]
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
        val state = profileUiState.tabMap[uri] ?: return null
        return unloadContent(state)
    }

}