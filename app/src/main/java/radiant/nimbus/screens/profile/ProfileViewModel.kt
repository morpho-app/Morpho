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
import app.bsky.feed.GetAuthorFeedFilter
import app.bsky.feed.GetAuthorFeedQueryParams
import app.bsky.graph.GetListsQueryParams
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.apiProvider
import radiant.nimbus.base.BaseViewModel
import radiant.nimbus.model.DetailedProfile
import radiant.nimbus.model.Skyline
import radiant.nimbus.model.toBskyPostList
import radiant.nimbus.model.toProfile
import radiant.nimbus.screens.destinations.PostThreadScreenDestination
import radiant.nimbus.screens.destinations.ProfileScreenDestination
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
    val apiProvider = app.apiProvider
    val api = apiProvider.api

    var state by mutableStateOf(ProfileState())
        private set

    private val _profilePosts = MutableStateFlow(Skyline(cursor = null))
    private val _profilePostsReplies = MutableStateFlow(Skyline(cursor = null))
    private val _profileMedia = MutableStateFlow(Skyline(cursor = null))

    val profilePosts: StateFlow<Skyline> = _profilePosts.asStateFlow()
    val profilePostsReplies: StateFlow<Skyline> = _profilePostsReplies.asStateFlow()
    val profileMedia: StateFlow<Skyline> = _profileMedia.asStateFlow()

    fun useCachedProfile(profile: DetailedProfile?) : Boolean {
        return if (profile != null) {
            state = ProfileState(AtIdentifier(profile.did.did), profile, false)
            true
        } else {
            false
        }
    }

    fun getProfile(
        actor: AtIdentifier,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        when (val result = api.getProfile(GetProfileQueryParams(actor))) {
            is AtpResponse.Failure -> {
                Log.e("P Load Err", result.toString())
                state = ProfileState(actor, null, isLoading = false, isError = true)
                onFailure()
            }

            is AtpResponse.Success -> {
                val profile = result.response.toProfile()
                Log.i("P Load Success", result.toString())
                state = ProfileState(actor, profile, false)
                getProfileFeed(ProfileTabs.Posts)
                onSuccess()
                getProfileFeed(ProfileTabs.PostsReplies)
                getProfileFeed(ProfileTabs.Media)

            }
        }
    }

    fun createRecord(
        record: RecordUnion,
    ) = CoroutineScope(Dispatchers.Default).launch {
        apiProvider.createRecord(record)
    }

    @Suppress("LocalVariableName")
    fun getProfileFeed(feed: ProfileTabs, cursor: String? = null, actor: AtIdentifier? = null
    ) = viewModelScope.launch(Dispatchers.IO) {
        val _actor = actor ?: state.actor
        if (_actor != null) {
            when (feed) {
                ProfileTabs.Posts -> {
                    val result = api.getAuthorFeed(GetAuthorFeedQueryParams(_actor, 100,cursor, GetAuthorFeedFilter.POSTS_NO_REPLIES))
                    if (result is AtpResponse.Success) {
                        val newPosts = Skyline.from(result.response.feed.toBskyPostList(), result.response.cursor)
                        if (cursor != null) {
                            Log.v("UpdatePosts", result.response.feed.toString())
                            _profilePosts.update { skyline -> Skyline.concat(skyline, newPosts) }
                        } else {
                            Log.v("Posts", result.response.feed.toString())
                            _profilePosts.value = newPosts

                        }
                    }

                }
                ProfileTabs.PostsReplies -> {
                    val result = api.getAuthorFeed(GetAuthorFeedQueryParams(_actor, 100, cursor, GetAuthorFeedFilter.POSTS_WITH_REPLIES))
                    if (result is AtpResponse.Success) {
                        Log.d("Posts+Replies", result.response.feed.toString())
                        val newPosts = Skyline.from(result.response.feed.toBskyPostList(), result.response.cursor)
                        if (cursor != null) {
                            Log.v("UpdatePosts+Replies", result.response.feed.toString())
                            _profilePostsReplies.update { skyline -> Skyline.concat(skyline, newPosts) }
                        } else {
                            Log.v("Posts+Replies", result.response.feed.toString())
                            _profilePostsReplies.value = newPosts

                        }
                    }
                }
                ProfileTabs.Media -> {
                    val result = api.getAuthorFeed(GetAuthorFeedQueryParams(_actor, 100, cursor, GetAuthorFeedFilter.POSTS_WITH_MEDIA))
                    if (result is AtpResponse.Success) {
                        Log.d("Media", result.response.feed.toString())
                        val newPosts = Skyline.from(result.response.feed.toBskyPostList(), result.response.cursor)
                        if (cursor != null) {
                            Log.v("UpdateMedia", result.response.feed.toString())
                            _profileMedia.update { skyline -> Skyline.concat(skyline, newPosts) }
                        } else {
                            Log.v("Media", result.response.feed.toString())
                            _profileMedia.value = newPosts

                        }
                    }
                }
                ProfileTabs.Feeds -> {
                    val result = api.getActorFeeds(GetActorFeedsQueryParams(_actor, 100))
                    if (result is AtpResponse.Success) {
                        Log.v("Feeds", result.response.feeds.toString())
                        //_profileMedia.value = Skyline.from(result.response.feed, profileMedia.value.cursor)
                    }
                }
                ProfileTabs.Lists -> {
                    val result = api.getLists(GetListsQueryParams(_actor, 100))
                    if (result is AtpResponse.Success) {
                        Log.v("Lists", result.response.lists.toString())
                        //_profileMedia.value = Skyline.from(result.response.feed, profileMedia.value.cursor)
                    }
                }
            }
        }

    }

    fun onItemClicked(uri: AtUri, navigator: DestinationsNavigator) {
        navigator.navigate(PostThreadScreenDestination(uri))
    }

    fun onProfileClicked(actor: AtIdentifier, navigator: DestinationsNavigator) {
        navigator.navigate(ProfileScreenDestination(actor))
    }
}