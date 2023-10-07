package radiant.nimbus.screens.skyline

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import app.bsky.feed.GetTimelineQueryParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.Cid
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.base.BaseViewModel
import radiant.nimbus.model.Skyline
import radiant.nimbus.model.ThreadPost
import javax.inject.Inject

data class SkylineState(
    val isLoading : Boolean = false
)

@HiltViewModel
class SkylineViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(SkylineState())
        private set

    private val _skylinePosts = MutableStateFlow(Skyline(emptyList(), null))
    val skylinePosts: StateFlow<Skyline> = _skylinePosts.asStateFlow()

    fun getSkyline(
        apiProvider: ApiProvider,
        cursor: String? = null,
    ) = viewModelScope.launch(Dispatchers.IO) {
        when(val result = apiProvider.api.getTimeline(GetTimelineQueryParams(limit = 60, cursor = cursor))) {
            is AtpResponse.Failure -> {
                Log.e("Timeline Load Err", result.toString())
            }

            is AtpResponse.Success -> {
                val newPosts = Skyline.from(result.response.feed, result.response.cursor)
                Log.i("Timeline Load Success", result.toString())
                if (cursor != null) {
                    Log.i("UpdatePosts", result.response.feed.toString())
                    _skylinePosts.update { skyline -> Skyline.concat(skyline, newPosts) }
                } else {
                    Log.i("Posts", result.response.feed.toString())
                    _skylinePosts.value = newPosts
                }
            }
        }
    }
}

fun filterSkylineThread(post: ThreadPost) : Cid? {
    return when(post) {
        is ThreadPost.BlockedPost -> null
        is ThreadPost.NotFoundPost -> null
        is ThreadPost.ViewablePost -> post.post.cid
    }
}