package com.morpho.app.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.bsky.actor.GetProfileQuery
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.bluesky.Profile
import com.morpho.app.model.bluesky.toProfile
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uidata.MorphoData
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.ContentLoadingState
import com.morpho.app.model.uistate.TabbedProfileScreenState
import com.morpho.app.model.uistate.UiLoadingState
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.auth.AtpUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
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




    fun initProfile() = screenModelScope.launch(Dispatchers.IO) {
        if(user != null) {
            userID = user.id
            myProfile = api.atpUser?.id == user.id
        } else {
            userID = api.atpUser?.id
            myProfile = true
        }
        log.d { "User ID: $userID"}
        profileUiState = profileUiState.copy(loadingState = UiLoadingState.Loading)
        user?.let { GetProfileQuery(it.id) }?.let {
            api.api.getProfile(it)
                .onSuccess {
                    loadProfile(it.toProfile())
                    log.d { "Profile loaded: ${it.toProfile()}" }
                }.onFailure {
                    profileUiState = profileUiState.copy(loadingState = UiLoadingState.Error("Profile not loaded"))
                    log.e(it) { "Profile not loaded. Error: $it" }
                }
        }

    }

    fun loadProfile(profile: DetailedProfile) = screenModelScope.launch(Dispatchers.IO) {
        profileState = ContentCardState.FullProfile(profile, loadingState = ContentLoadingState.Loading)
        val profileEntry = ContentCardMapEntry.Profile(profile.did)
        profileState = initProfileContent(profileEntry, force = true, fill = true).first()
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