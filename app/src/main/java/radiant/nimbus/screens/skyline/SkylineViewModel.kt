package radiant.nimbus.screens.skyline

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import app.bsky.feed.GetFeedQueryParams
import app.bsky.feed.GetTimelineQueryParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.base.BaseViewModel
import radiant.nimbus.model.Skyline
import javax.inject.Inject

data class SkylineState(
    val isLoading : Boolean = false,
    val feedUri: AtUri? = null
)

@HiltViewModel
class SkylineViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(SkylineState())
        private set

    private val _skylinePosts = MutableStateFlow(Skyline(emptyList(), null))
    val skylinePosts: StateFlow<Skyline> = _skylinePosts.asStateFlow()


    fun createRecord(
        record: RecordUnion,
        apiProvider: ApiProvider,
    ) = CoroutineScope(Dispatchers.Default).launch {
        apiProvider.createRecord(record)
    }

    fun getSkyline(
        apiProvider: ApiProvider,
        cursor: String? = null,
        limit: Long = 60,
    ) = viewModelScope.launch(Dispatchers.IO) {
        when(val result = apiProvider.api.getTimeline(GetTimelineQueryParams(limit = limit, cursor = cursor))) {
            is AtpResponse.Failure -> {
                Log.e("Timeline Load Err", result.toString())
            }

            is AtpResponse.Success -> {
                val newPosts = Skyline.from(result.response.feed, result.response.cursor)
                Log.i("Timeline Load Success", result.toString())
                if (cursor != null) {
                    Log.i("UpdatePosts", result.response.feed.toString())
                    _skylinePosts.update { skyline -> Skyline.concat(skyline, newPosts.collectThreads().await()) }
                } else {
                    Log.i("Posts", result.response.feed.toString())
                    if(skylinePosts.value.posts.isNotEmpty() && (Clock.System.now().epochSeconds - skylinePosts.value.posts.first().post?.createdAt?.instant?.epochSeconds!! > 10)) {
                        _skylinePosts.update { skyline -> Skyline.concat(newPosts.collectThreads().await(), skyline, null) }
                    } else {
                        _skylinePosts.value = newPosts
                        _skylinePosts.update { skyline -> skyline.collectThreads().await() }
                    }
                }
            }
        }
    }

    fun getSkyline(
        apiProvider: ApiProvider,
        feedQuery: GetFeedQueryParams,
        cursor: String? = null,
    ) = viewModelScope.launch(Dispatchers.IO) {
        val result = if(cursor == null) apiProvider.api.getFeed(feedQuery) else {
            apiProvider.api.getFeed(feedQuery.copy(cursor = cursor))
        }
        when(result) {
            is AtpResponse.Failure -> {
                Log.e("Timeline Load Err", result.toString())
            }

            is AtpResponse.Success -> {
                val newPosts = Skyline.from(result.response.feed, result.response.cursor)
                Log.i("Timeline Load Success", result.toString())
                if (cursor != null || feedQuery.cursor != null) {
                    Log.i("UpdatePosts", result.response.feed.toString())
                    _skylinePosts.update { skyline -> Skyline.concat(skyline, newPosts.collectThreads().await()) }
                } else {
                    if(result.response.feed.first().post.indexedAt.toEpochMilliseconds() >
                        (skylinePosts.value.posts.first().post?.indexedAt?.instant?.toEpochMilliseconds() ?: 0)
                    ) {
                        Log.i("UpdatePosts", result.response.feed.toString())
                        _skylinePosts.update { skyline -> Skyline.concat(newPosts.collectThreads().await(), skyline, null) }
                    } else {
                        Log.i("Posts", result.response.feed.toString())
                        _skylinePosts.value = newPosts
                        _skylinePosts.update { skyline -> skyline.collectThreads().await() }
                    }
                }
            }
        }
    }
}