package com.morpho.app.model.uidata

import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastMap
import app.bsky.actor.*
import app.bsky.graph.*
import app.bsky.labeler.GetServicesQuery
import app.bsky.labeler.GetServicesResponseViewUnion
import com.morpho.app.data.AccessibilityPreferences
import com.morpho.app.data.BskyUserPreferences
import com.morpho.app.data.PreferencesRepository
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uistate.NotificationsFilterState
import com.morpho.butterfly.*
import com.morpho.butterfly.model.RecordType
import com.morpho.butterfly.model.RecordUnion
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging
import kotlin.collections.List


@OptIn(ExperimentalCoroutinesApi::class)
class SettingsService: KoinComponent {
    companion object {
        val log = logging()
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    val api: Butterfly by inject()
    val prefs: PreferencesRepository by inject()


    private var _currentUser: MutableStateFlow<BskyUser?> = MutableStateFlow(null)
    private var _currentUserPrefs: MutableStateFlow<BskyUserPreferences?> = MutableStateFlow(null)

    val currentUser = _currentUser.asStateFlow()
    var currentUserPrefs = _currentUserPrefs.asStateFlow()


    val languages: Flow<List<Language>> = currentUserPrefs.transform {
        if(it != null) emit(it.preferences.languages)
    }

    val notificationsFilter: Flow<NotificationsFilterState> = currentUserPrefs.transform {
        if(it?.morphoPrefs?.notificationsFilter != null) emit(it.morphoPrefs.notificationsFilter)
    }

    val threadViewPrefs: Flow<ThreadViewPref> = currentUserPrefs.transform {
        if(it?.preferences?.threadViewPrefs != null) emit(it.preferences.threadViewPrefs!!)
    }

    val feedViewPrefs: Flow<Map<String, BskyFeedPref>> = currentUserPrefs.transform {
        if(it?.preferences?.feedViewPrefs != null) emit(it.preferences.feedViewPrefs)
    }

    val mergeFeeds: Flow<Boolean> = currentUserPrefs.transform {
        if(it?.preferences?.mergeFeeds != null) emit(it.preferences.mergeFeeds)
    }

    val contentLabelPrefs: Flow<List<ContentLabelPref>> = currentUserPrefs.transform {
        if(it?.preferences?.contentLabelPrefs != null) emit(it.preferences.contentLabelPrefs)
    }

    val mutedWords: Flow<List<MutedWord>> = currentUserPrefs.transform {
        if(it?.preferences?.mutedWords != null) emit(it.preferences.mutedWords)
    }

    val mutedUsers: Flow<List<BasicProfile>> = currentUserPrefs.transform {
        if(it?.preferences?.mutes != null) emit(it.preferences.mutes)
    }

    val hiddenPosts: Flow<List<AtUri>> = currentUserPrefs.transform {
        if(it?.preferences?.hiddenPosts != null) emit(it.preferences.hiddenPosts)
    }

    val showAdultContent: Flow<Boolean> = currentUserPrefs.transform {
        if(it?.preferences?.adultContent?.enabled != null) emit(it.preferences.adultContent?.enabled ?: false)
    }

    val savedFeeds: Flow<List<UISavedFeed>> = currentUserPrefs.transform { preferences ->
        if(preferences?.preferences?.savedFeeds != null) emit(preferences.preferences.savedFeeds!!.items.map { it.toUISavedFeed(api) })
    }
    val pinnedFeeds: Flow<List<UISavedFeed>> = currentUserPrefs.transform { preferences ->
        if(preferences?.preferences?.savedFeeds != null)
            emit(preferences.preferences.savedFeeds!!.items.filter { it.pinned }.map {
                it.toUISavedFeed(api)
            })
    }

    val labelers: Flow<List<BskyLabelService>> = currentUserPrefs.transformLatest { preferences ->
        if (preferences?.preferences?.labelers?.isNotEmpty() == true)
            emit(preferences.preferences.labelers.toImmutableList()
                .let { labelerList -> GetServicesQuery(labelerList) }.let { query ->
                    api.api.getServices(query)
                        .map { resp ->
                            resp.views.map { service ->
                                when(service) {
                                    is GetServicesResponseViewUnion.LabelerView ->
                                        service.value.toLabelService()
                                    is GetServicesResponseViewUnion.LabelerViewDetailed ->
                                        service.value.toLabelService()
                                }
                            }
                        }.getOrNull()
                } ?: emptyList())
    }


    init {
        serviceScope.launch {
            while(!api.isLoggedIn()) {
                delay(100)
            }
            _currentUser.value = api.atpUser?.let { prefs.getUser(it.id).getOrNull() }
            if(_currentUser.value != null && _currentUserPrefs.value == null) {
                _currentUserPrefs.value = prefs.getFullPrefsLocal(api.atpUser!!.id).getOrNull() ?:
                    prefs.getFullPrefsRemote(api.atpUser!!.id).getOrNull()
//                currentUserPrefs = prefs.userPrefs(api.atpUser!!.id).stateIn(
//                    serviceScope,
//                    SharingStarted.Eagerly,
//                    prefs.getFullPrefsLocal(api.atpUser!!.id).getOrNull()
//                )
            }
        }
    }

    fun setUser(id: AtIdentifier) = serviceScope.launch {
        _currentUser.value = prefs.getUser(id).getOrNull()
        api.switchUser(id)
        if(_currentUser.value != null) {
            currentUserPrefs = prefs.userPrefs(id).stateIn(
                serviceScope,
                SharingStarted.Eagerly,
                prefs.getFullPrefsRemote(id).getOrNull()
            )
        }
        delay(10000)
        if(_currentUserPrefs.value != null) {
            currentUserPrefs = _currentUserPrefs.asStateFlow()
        }
    }

    fun setAccessibilityPrefs(newPrefs: AccessibilityPreferences) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs.updateAndGet {
            it?.copy(morphoPrefs = it.morphoPrefs.copy(accessibility = newPrefs))
        }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun setNotificationsPrefs(newPrefs: NotificationsFilterState) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs.updateAndGet {
            it?.copy(morphoPrefs = it.morphoPrefs.copy(notificationsFilter = newPrefs))
        }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun setThreadViewPrefs(newPrefs: ThreadViewPref) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs.updateAndGet {
            it?.copy(preferences = it.preferences.copy(threadViewPrefs = newPrefs))
        }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun toggleMergeFeeds() = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs.updateAndGet {
            it?.copy(preferences = it.preferences.copy(mergeFeeds = !it.preferences.mergeFeeds))
        }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun addMutedWord(newWord: MutedWord) = serviceScope.launch {
        val sanitizedMuteWord = newWord.copy(value = newWord.value.trim().replace(
            "/^#(?!\\ufe0f)/", ""
        ).replace("/[\\r\\n\\u00AD\\u2060\\u200D\\u200C\\u200B]+/", ""))
        val updatedPrefs = _currentUserPrefs
            .updateAndGet {
                it?.copy(preferences = it.preferences.copy(
                    mutedWords = it.preferences.mutedWords + sanitizedMuteWord
                ))
            }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun removeMutedWord(word: MutedWord) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs
            .updateAndGet { preferences ->
                preferences?.copy(preferences = preferences.preferences.copy(
                    mutedWords = preferences.preferences.mutedWords.filterNot { it == word }
                ))
            }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun addMutedUser(newMute: BasicProfile) = serviceScope.launch {
         _currentUserPrefs.update {
            it?.copy(preferences = it.preferences.copy(mutes = it.preferences.mutes + newMute))
        }
        api.api.muteActor(MuteActorRequest(newMute.did))
    }

    fun muteUserList(newMute: AtUri) = serviceScope.launch {
        val list = api.api.getList(GetListQuery(newMute)).getOrNull() ?: return@launch
        _currentUserPrefs.update { preferences ->
            preferences?.copy(preferences = preferences.preferences.copy(
                mutes = preferences.preferences.mutes + list.list.items.map { it.toProfile() as BasicProfile}))
        }
        api.api.muteActorList(MuteActorListRequest(newMute))
    }

    fun removeMutedUser(oldMute: BasicProfile) = serviceScope.launch {
        _currentUserPrefs.update { preferences ->
                preferences?.copy(preferences = preferences.preferences.copy(
                    mutes = preferences.preferences.mutes.filterNot { it.did == oldMute.did }
                ))
            }
        api.api.unmuteActor(UnmuteActorRequest(oldMute.did))
    }

    fun unmuteUserList(oldMute: AtUri) = serviceScope.launch {
        val list = api.api.getList(GetListQuery(oldMute)).getOrNull() ?: return@launch
        _currentUserPrefs.update { preferences ->
            preferences?.copy(preferences = preferences.preferences.copy(
                mutes = preferences.preferences.mutes.filterNot { prefMut ->
                    list.list.items.fastAny {
                        it.did == prefMut.did
                    } }
            ))
        }
        api.api.unmuteActorList(UnmuteActorListRequest(oldMute))
    }

    fun hidePost(post: AtUri) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs
            .updateAndGet { preferences ->
                preferences?.copy(preferences = preferences.preferences.copy(
                    hiddenPosts = preferences.preferences.hiddenPosts + post
                ))
            }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun unhidePost(post: AtUri) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs
            .updateAndGet { preferences ->
                preferences?.copy(preferences = preferences.preferences.copy(
                    hiddenPosts = preferences.preferences.hiddenPosts.filterNot { it == post }
                ))
            }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun setAdultContentPref(showAdultContent: Boolean) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs.updateAndGet { prefs ->
            prefs?.copy(preferences = prefs.preferences.copy(adultContent = AdultContentPref(showAdultContent)))
        }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun addSavedFeed(newFeed: SavedFeed) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs
            .updateAndGet {
                it?.copy(preferences = it.preferences.copy(
                    savedFeeds = it.preferences.savedFeeds?.copy(
                        items = (it.preferences.savedFeeds!!.items + newFeed).toPersistentList())))
            }

        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun addSavedFeeds(newFeeds: List<SavedFeed>) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs
            .updateAndGet {
                it?.copy(preferences = it.preferences.copy(
                    savedFeeds = it.preferences.savedFeeds?.copy(
                        items = (it.preferences.savedFeeds!!.items + newFeeds).toPersistentList())))
            }

        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }


    fun updateSavedFeed(newFeed: SavedFeed) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs
            .updateAndGet { preferences ->
                preferences?.copy(preferences = preferences.preferences.copy(
                    savedFeeds = preferences.preferences.savedFeeds?.copy(
                        items = (preferences.preferences.savedFeeds!!.items.fastMap {
                            if (it.value == newFeed.value) newFeed else it
                        }).toPersistentList())))
            }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun removeSavedFeed(uri: AtUri) = serviceScope.launch {
        val updatedPrefs = _currentUserPrefs
            .updateAndGet { preferences ->
                preferences?.copy(preferences = preferences.preferences.copy(
                    savedFeeds = preferences.preferences.savedFeeds?.copy(
                        items = (preferences.preferences.savedFeeds!!.items.filter {
                            it.value != uri.toString()
                        }).toPersistentList())))
            }
        if (updatedPrefs != null) {
            prefs.setPreferencesRemote(currentUser.value!!, updatedPrefs.preferences, updatedPrefs.morphoPrefs)
        }
    }

    fun blockUser(user: BasicProfile) = serviceScope.launch {
        api.createRecord(RecordUnion.Block(user.did))
    }

    fun unblockUser(user: Did) = serviceScope.launch {
        val profile = api.api.getProfile(GetProfileQuery(user)).getOrNull() ?: return@launch
        api.deleteRecord(RecordType.Block, profile.viewer?.blocking)
    }

    fun followUser(user: Did) = serviceScope.launch {
        api.createRecord(RecordUnion.Follow(user))
    }

    fun unfollowUser(user: Did) = serviceScope.launch {
        val profile = api.api.getProfile(GetProfileQuery(user)).getOrNull() ?: return@launch
        api.deleteRecord(RecordType.Follow, profile.viewer?.following)
    }


}