package radiant.nimbus.screens.profile

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import app.bsky.actor.GetProfileQueryParams
import app.bsky.feed.GetActorFeedsQueryParams
import app.bsky.feed.GetAuthorFeedQueryParams
import app.bsky.graph.GetListsQueryParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.base.BaseViewModel
import radiant.nimbus.model.DetailedProfile
import radiant.nimbus.model.Skyline
import radiant.nimbus.model.toProfile
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.response.AtpResponse
import javax.inject.Inject


data class ProfileState(
    var actor: AtIdentifier? = null,
    var profile: DetailedProfile? = null,
    var isLoading: Boolean = true,
    var isError: Boolean = false,
) {
}

enum class ProfileTabs {
    Posts,
    PostsReplies,
    Media,
    Feeds,
    Lists,
}



@HiltViewModel
class ProfileViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(ProfileState())
        private set

    private val _profilePosts = MutableStateFlow(Skyline(emptyList(), null))
    private val _profilePostsReplies = MutableStateFlow(Skyline(emptyList(), null))
    private val _profileMedia = MutableStateFlow(Skyline(emptyList(), null))

    val profilePosts: StateFlow<Skyline> = _profilePosts.asStateFlow()
    val profilePostsReplies: StateFlow<Skyline> = _profilePostsReplies.asStateFlow()
    val profileMedia: StateFlow<Skyline> = _profileMedia.asStateFlow()


    fun getProfile(
        apiProvider: ApiProvider,
        actor: AtIdentifier,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        when(val result = apiProvider.api.getProfile(GetProfileQueryParams(actor))) {
            is AtpResponse.Failure -> {
                Log.e("P Load Err", result.toString())
                state = ProfileState(actor, null, isLoading = false, isError = true)
                onFailure()
            }

            is AtpResponse.Success -> {
                val profile = result.response.toProfile()
                Log.i("P Load Success", result.toString())
                state = ProfileState(actor,profile,false)
                getProfileFeed(ProfileTabs.Posts, apiProvider)
                onSuccess()
            }
        }
    }

    fun getProfileFeed(feed: ProfileTabs, apiProvider: ApiProvider) = viewModelScope.launch(Dispatchers.IO) {
        if (state.actor != null) {
            when (feed) {
                ProfileTabs.Posts -> {
                    val result = apiProvider.api.getAuthorFeed(GetAuthorFeedQueryParams(state.actor!!, 100, profilePosts.value.cursor))
                    if (result is AtpResponse.Success) {
                        Log.i("Feeds", result.response.feed.toString())
                        _profilePosts.value = Skyline.from(result.response.feed, profilePosts.value.cursor)
                    }

                }
                ProfileTabs.PostsReplies -> {
                    val result = apiProvider.api.getAuthorFeed(GetAuthorFeedQueryParams(state.actor!!, 100, profilePostsReplies.value.cursor))
                    if (result is AtpResponse.Success) {
                        Log.i("Feeds", result.response.feed.toString())
                        _profilePostsReplies.value = Skyline.from(result.response.feed, profilePostsReplies.value.cursor)
                    }
                }
                ProfileTabs.Media -> {
                    val result = apiProvider.api.getAuthorFeed(GetAuthorFeedQueryParams(state.actor!!, 100, profileMedia.value.cursor))
                    if (result is AtpResponse.Success) {
                        Log.i("Feeds", result.response.feed.toString())
                        _profileMedia.value = Skyline.from(result.response.feed, profileMedia.value.cursor)
                    }
                }
                ProfileTabs.Feeds -> {
                    val result = apiProvider.api.getActorFeeds(GetActorFeedsQueryParams(state.actor!!, 100))
                    if (result is AtpResponse.Success) {
                        Log.i("Feeds", result.response.feeds.toString())
                        //_profileMedia.value = Skyline.from(result.response.feed, profileMedia.value.cursor)
                    }
                }
                ProfileTabs.Lists -> {
                    val result = apiProvider.api.getLists(GetListsQueryParams(state.actor!!, 100))
                    if (result is AtpResponse.Success) {
                        Log.i("Lists", result.response.lists.toString())
                        //_profileMedia.value = Skyline.from(result.response.feed, profileMedia.value.cursor)
                    }
                }
            }
        }

    }
}