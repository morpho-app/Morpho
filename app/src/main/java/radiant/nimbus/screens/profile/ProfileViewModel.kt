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
import io.ktor.client.*
import io.ktor.client.engine.cio.*


data class ProfileState(
    var actor: AtIdentifier = AtIdentifier(""),
    var profile: DetailedProfile? = null,
) {
}




@HiltViewModel
class ProfileViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(ProfileState())
        private set

    var client = XrpcBlueskyApi(httpClient = HttpClient(CIO))

    suspend fun getProfile(actor: AtIdentifier? = null): DetailedProfile? {
        if (actor != null) {
            state.actor = actor
        }
        return when (val result = client.getProfile(GetProfileQueryParams(state.actor))) {
            is AtpResponse.Failure -> null
            is AtpResponse.Success -> {
                state.profile = result.response.toProfile()
                state.profile
            }
        }
    }


    suspend fun getProfilePosts(): ImmutableList<FeedViewPost>? {

        Log.i("Feed", "getting feed")
        return when (val result = client.getAuthorFeed(GetAuthorFeedQueryParams(state.actor, 100))) {
            is AtpResponse.Failure -> {
                Log.e("Feed", "Failed to get feed:" + result.error.toString())
                null
            }

            is AtpResponse.Success -> {
                Log.i("Feed", result.response.feed.toString())
                result.response.feed
            }
        }

    }
}