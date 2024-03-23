package com.morpho.app.screens.profile

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import app.bsky.actor.GetProfileQuery
import app.bsky.feed.GetActorFeedsQuery
import app.bsky.feed.GetAuthorFeedFilter
import app.bsky.feed.GetAuthorFeedQuery
import app.bsky.graph.GetListsQuery
import com.morpho.app.butterfly
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordUnion
import com.morpho.app.base.BaseViewModel
import com.morpho.app.model.DetailedProfile
import com.morpho.app.model.Skyline
import com.morpho.app.model.toBskyPostList
import com.morpho.app.model.toProfile
import com.morpho.app.screens.destinations.PostThreadScreenDestination
import com.morpho.app.screens.destinations.ProfileScreenDestination
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
    val apiProvider = app.butterfly
    val api = app.butterfly.api

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
            state = ProfileState(profile.did, profile, false)
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
        api.getProfile(GetProfileQuery(actor)).onFailure {
            Log.e("P Load Err", it.toString())
            state = ProfileState(actor, null, isLoading = false, isError = true)
            onFailure()
        }.onSuccess {
            val profile = it.toProfile()
            Log.i("P Load Success", it.toString())
            state = ProfileState(actor, profile, false)
            getProfileFeed(ProfileTabs.Posts)
            onSuccess()
            getProfileFeed(ProfileTabs.PostsReplies)
            getProfileFeed(ProfileTabs.Media)
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
        if (_actor != null) runCatching {
            when (feed) {
                ProfileTabs.Posts -> {
                    api.getAuthorFeed(GetAuthorFeedQuery(_actor, 100,cursor, GetAuthorFeedFilter.POSTS_NO_REPLIES))
                        .onSuccess {
                            val newPosts = Skyline.from(it.feed.toBskyPostList(), it.cursor)
                            if (cursor != null) {
                                Log.v("UpdatePosts", it.feed.toString())
                                _profilePosts.update { skyline -> Skyline.concat(skyline, newPosts) }
                            } else {
                                Log.v("Posts", it.feed.toString())
                                _profilePosts.value = newPosts

                            }
                        }
                }
                ProfileTabs.PostsReplies -> {
                    api.getAuthorFeed(GetAuthorFeedQuery(_actor, 100, cursor, GetAuthorFeedFilter.POSTS_WITH_REPLIES))
                        .onSuccess {
                            Log.d("Posts+Replies", it.feed.toString())
                            val newPosts = Skyline.from(it.feed.toBskyPostList(), it.cursor)
                            if (cursor != null) {
                                Log.v("UpdatePosts+Replies", it.feed.toString())
                                _profilePostsReplies.update { skyline -> Skyline.concat(skyline, newPosts) }
                            } else {
                                Log.v("Posts+Replies", it.feed.toString())
                                _profilePostsReplies.value = newPosts

                            }
                        }
                }
                ProfileTabs.Media -> {
                    api.getAuthorFeed(GetAuthorFeedQuery(_actor, 100, cursor, GetAuthorFeedFilter.POSTS_WITH_MEDIA))
                        .onSuccess {
                            Log.d("Media", it.feed.toString())
                            val newPosts = Skyline.from(it.feed.toBskyPostList(), it.cursor)
                            if (cursor != null) {
                                Log.v("UpdateMedia", it.feed.toString())
                                _profileMedia.update { skyline -> Skyline.concat(skyline, newPosts) }
                            } else {
                                Log.v("Media", it.feed.toString())
                                _profileMedia.value = newPosts

                            }
                        }
                }
                ProfileTabs.Feeds -> {
                    api.getActorFeeds(GetActorFeedsQuery(_actor, 100)).onSuccess {
                        Log.v("Feeds", it.feeds.toString())
                        //_profileMedia.value = Skyline.from(it.feed, profileMedia.value.cursor)
                    }
                }
                ProfileTabs.Lists -> {
                    api.getLists(GetListsQuery(_actor, 100)).onSuccess {
                        Log.v("Lists", it.lists.toString())
                        //_profileMedia.value = Skyline.from(it.feed, profileMedia.value.cursor)
                    }
                }
            }
        }.recover {
            Log.e("Profile", it.toString())
        }

    }

    fun onItemClicked(uri: AtUri, navigator: DestinationsNavigator) {
        navigator.navigate(PostThreadScreenDestination(uri))
    }

    fun onProfileClicked(actor: AtIdentifier, navigator: DestinationsNavigator) {
        navigator.navigate(ProfileScreenDestination(actor))
    }
}