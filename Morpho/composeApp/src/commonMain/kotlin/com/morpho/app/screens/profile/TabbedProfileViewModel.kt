package com.morpho.app.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.bsky.actor.GetProfileQuery
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uidata.MorphoData
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.TabbedProfileScreenState
import com.morpho.app.model.uistate.UiLoadingState
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.butterfly.AtIdentifier
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

@Suppress("UNCHECKED_CAST")
// TODO: Revisit these casts if we can, but they should be safe
class TabbedProfileViewModel(
    val id: AtIdentifier? = null
): MainScreenModel() {

    companion object {
        val log = logging()
    }
    var profileUiState: TabbedProfileScreenState by mutableStateOf(
        TabbedProfileScreenState(loadingState = UiLoadingState.Loading))
        private set

    var profileState: ContentCardState.FullProfile<Profile>? by mutableStateOf(null)
        private set

    private val tabs = mutableListOf<ContentCardMapEntry>()

    private val _tabFlow = MutableStateFlow(tabs.toList())
    val tabFlow: StateFlow<List<ContentCardMapEntry>>
        get() = _tabFlow.asStateFlow()

    var profileId: AtIdentifier?  by mutableStateOf(null)
        private set

    var myProfile: Boolean = false
        private set




    fun initProfile() = screenModelScope.launch {
        if(initialized) return@launch
        init(false)
        if(id != null) {
            profileId = id
            myProfile = api.atpUser?.id == id
        } else {
            profileId = api.atpUser?.id
            myProfile = true
        }
        log.d { "Profile of: $profileId"}
        initialized = true
        if(profileId == null) {
            profileUiState = profileUiState.copy(
                loadingState = UiLoadingState.Error("Profile not found")
            )
            return@launch
        }

        profileId?.let { GetProfileQuery(it) }?.let { query ->
            api.api.getProfile(query)
                .onSuccess { resp ->
                    profileState = loadProfile(resp.toProfile())
                    log.d { "Profile loaded: ${resp.toProfile()}" }
                    if (profileState != null) {
                        val tabStates = mutableListOf<StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>>()
                        when(profileState!!.profile) {
                            is DetailedProfile -> {
                                if (profileState?.postsState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.Feed(
                                            profileState!!.postsState.value!!.uri,
                                            profileState!!.postsState.value!!.feed.title,
                                            cursors[profileState!!.postsState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.postsState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                                if (profileState?.postRepliesState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.PostThread(
                                            profileState!!.postRepliesState.value!!.uri,
                                            profileState!!.postRepliesState.value!!.feed.title,
                                            cursors[profileState!!.postRepliesState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.postRepliesState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                                if (profileState?.mediaState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.Feed(
                                            profileState!!.mediaState.value!!.uri,
                                            profileState!!.mediaState.value!!.feed.title,
                                            cursors[profileState!!.mediaState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.mediaState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                                if(myProfile && profileState?.likesState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.Feed(
                                            profileState!!.likesState.value!!.uri,
                                            profileState!!.likesState.value!!.feed.title,
                                            cursors[profileState!!.likesState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.likesState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                                if (profileState?.feedsState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.FeedList(
                                            profileState!!.feedsState.value!!.uri,
                                            profileState!!.feedsState.value!!.feed.title,
                                            cursors[profileState!!.feedsState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.feedsState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                                if (profileState?.listsState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.UserList(
                                            profileState!!.listsState.value!!.uri,
                                            profileState!!.listsState.value!!.feed.title,
                                            cursors[profileState!!.listsState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.listsState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                            }
                            is BskyLabelService -> {
                                if (profileState?.modServiceState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.ServiceList(
                                            profileState!!.modServiceState.value!!.uri,
                                            profileState!!.modServiceState.value!!.feed.title,
                                            cursors[profileState!!.modServiceState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.modServiceState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                                if (profileState?.listsState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.UserList(
                                            profileState!!.listsState.value!!.uri,
                                            profileState!!.listsState.value!!.feed.title,
                                            cursors[profileState!!.listsState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.listsState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                                if (profileState?.postsState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.Feed(
                                            profileState!!.postsState.value!!.uri,
                                            profileState!!.postsState.value!!.feed.title,
                                            cursors[profileState!!.postsState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.postsState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                                if (profileState?.postRepliesState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.PostThread(
                                            profileState!!.postRepliesState.value!!.uri,
                                            profileState!!.postRepliesState.value!!.feed.title,
                                            cursors[profileState!!.postRepliesState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.postRepliesState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                                if (profileState?.feedsState != null) {
                                    tabs.add(
                                        ContentCardMapEntry.FeedList(
                                            profileState!!.feedsState.value!!.uri,
                                            profileState!!.feedsState.value!!.feed.title,
                                            cursors[profileState!!.feedsState.value!!.uri]
                                                ?: MutableStateFlow(null)
                                        )
                                    )
                                    tabStates.add(profileState!!.feedsState as StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>)
                                }
                            }
                            else -> {}
                        }
                        _tabFlow.value = tabs.toImmutableList()
                        log.d { "Tabs: ${tabs.map { it.title }}"}
                        profileUiState = profileUiState.copy(
                            tabs = tabFlow,
                            tabStates = tabStates.toImmutableList(),
                            loadingState = UiLoadingState.Idle
                        )
                    }
                }.onFailure {
                    profileUiState = profileUiState
                        .copy(
                            loadingState = UiLoadingState
                            .Error("Profile not loaded")
                        )
                    log.e(it) { "Profile not loaded. Error: $it" }
                }
        }

    }

    suspend fun loadProfile(profile: DetailedProfile): ContentCardState.FullProfile<Profile>? {
        val profileEntry = ContentCardMapEntry.Profile(profile.did)
        return initProfileContent(profileEntry, force = true, fill = true).first()
    }


    override fun unloadContent(entry: ContentCardMapEntry): MorphoData<MorphoDataItem>? {
        val maybeTab = profileUiState.tabMap[entry.uri]
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
        val state = profileUiState.tabMap[uri] ?: return null
        return unloadContent(state)
    }

}