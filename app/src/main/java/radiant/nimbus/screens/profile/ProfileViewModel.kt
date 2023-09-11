package radiant.nimbus.screens.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import radiant.nimbus.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import sh.christian.ozone.api.Did
import kotlinx.coroutines.*
import app.bsky.actor.*
import app.bsky.feed.FeedViewPost
import app.bsky.feed.GetAuthorFeedQueryParams
import app.bsky.feed.GetPostsQueryParams
import io.ktor.client.HttpClient
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import radiant.nimbus.model.DetailedProfile
import radiant.nimbus.model.toProfile
import radiant.nimbus.session.BlueskySession
import sh.christian.ozone.XrpcBlueskyApi
import sh.christian.ozone.api.AtIdentifier
import sh.christian.ozone.api.Handle
import sh.christian.ozone.api.response.AtpResponse
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.collections.immutable.immutableListOf
import io.ktor.client.engine.okhttp.*
import radiant.nimbus.screens.skyline.SkylineState


data class ProfileState(
    val actor: AtIdentifier? = null,
    val profile: DetailedProfile? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
) {
}




@HiltViewModel
class ProfileViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {
    var state by mutableStateOf(ProfileState())
        private set

    init {

    }
}