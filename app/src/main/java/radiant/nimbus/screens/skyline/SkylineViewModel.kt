package radiant.nimbus.screens.skyline

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import app.bsky.feed.GetFeedQueryParams
import app.bsky.feed.GetTimelineQueryParams
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.BskyFeedPref
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.apiProvider
import radiant.nimbus.base.BaseViewModel
import radiant.nimbus.model.BasicProfile
import radiant.nimbus.model.Skyline
import radiant.nimbus.model.Skyline.Companion.filterByPrefs
import radiant.nimbus.model.TunerFunction
import radiant.nimbus.model.toBskyPostList
import radiant.nimbus.model.tune
import radiant.nimbus.screens.destinations.PostThreadScreenDestination
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import javax.inject.Inject

data class SkylineState(
    val isLoading : Boolean = false,
    val feedUri: AtUri? = null,
    val hasNewPosts: Boolean = false,
)

private const val TAG = "Skyline"

@HiltViewModel
class SkylineViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {
    val apiProvider = app.apiProvider
    var state by mutableStateOf(SkylineState())
        private set

    private val _skylinePosts = MutableStateFlow(Skyline(cursor = null))
    val skylinePosts: StateFlow<Skyline> = _skylinePosts.asStateFlow()
    val feedPosts = mutableStateMapOf(Pair(AtUri(""),MutableStateFlow( Skyline(cursor = null))))
    var pinnedFeeds = mutableStateListOf<FeedTab>()

    fun hasPosts() = viewModelScope.async {
        if (state.hasNewPosts) return@async
        launch(Dispatchers.IO) {
            when(val result = apiProvider.api.getTimeline(GetTimelineQueryParams(limit = 1, cursor = null))) {
                is AtpResponse.Failure -> {}
                is AtpResponse.Success -> {
                    if (result.response.feed.isNotEmpty()) {
                        val cid = result.response.feed.first().post.cid
                        if (!skylinePosts.value.contains(cid)) {
                            _skylinePosts.update { it.copy(hasNewPosts = true) }
                            state = state.copy(hasNewPosts = true)
                            return@launch
                        }
                    }
                }
            }
        }.join()
        pinnedFeeds.fastMap {
            launch(Dispatchers.IO) {
                when(val result = apiProvider.api.getFeed(GetFeedQueryParams(limit = 1, cursor = null, feed = it.uri))) {
                    is AtpResponse.Failure -> {}
                    is AtpResponse.Success -> {
                        if (result.response.feed.isNotEmpty()) {
                            val cid = result.response.feed.first().post.cid
                            if (feedPosts[it.uri]?.value?.contains(cid) != true) {
                                state = state.copy(hasNewPosts = true)
                                feedPosts[it.uri]?.update { it.copy(hasNewPosts = true) }
                                return@launch
                            }
                        }
                    }
                }
            }
        }.joinAll()
    }

    fun createRecord(
        record: RecordUnion,
    ) = CoroutineScope(Dispatchers.Default).launch {
        apiProvider.createRecord(record)
    }

    fun getSkyline(
        cursor: String? = null,
        limit: Long = 100,
        prefs: BskyFeedPref = BskyFeedPref(),
        follows: List<BasicProfile> = listOf(),
    ) = viewModelScope.launch(Dispatchers.IO) {
        when(val result = apiProvider.api.getTimeline(GetTimelineQueryParams(limit = limit, cursor = cursor))) {
            is AtpResponse.Failure -> {
                Log.e(TAG, "Load Err $result")
            }

            is AtpResponse.Success -> {
                val tuners = mutableListOf<TunerFunction>()
                tuners.add { posts -> filterByPrefs(posts, prefs, follows.fastMap { it.did }) }

                val newPosts = result.response.feed.toBskyPostList().tune(tuners)


                if (cursor != null) {
                    Log.v(TAG, "UpdatePosts:, ${result.response.feed}")
                    _skylinePosts.update { skyline -> Skyline.concat(skyline, Skyline.collectThreads(apiProvider,result.response.cursor,newPosts).await()) }//Skyline.collectThreads(apiProvider, result.response.cursor, newPosts).await()) }
                    //_skylinePosts.update { Skyline.collectThreads(apiProvider,result.response.cursor,newPosts).await() }
                } else {
                    Log.v(TAG,"Posts: ${result.response.feed}")
                    //_skylinePosts.update { Skyline.from(newPosts, result.response.cursor) }
                    _skylinePosts.emit(Skyline.collectThreads(apiProvider,result.response.cursor,newPosts).await())
                }
            }
        }
    }

    fun getSkyline(
        feedQuery: GetFeedQueryParams,
        cursor: String? = null,
    ) = viewModelScope.launch(Dispatchers.IO) {
        val result = if(cursor == null) apiProvider.api.getFeed(feedQuery) else {
            apiProvider.api.getFeed(feedQuery.copy(cursor = cursor))
        }
        when(result) {
            is AtpResponse.Failure -> {
                Log.e(TAG, "Feed Load Err $result")
            }

            is AtpResponse.Success -> {
                val newPosts = Skyline.from(result.response.feed.toBskyPostList(), result.response.cursor)
                if (result.response.feed.isNotEmpty() ){
                    if (cursor != null || feedQuery.cursor != null) {
                        Log.v(TAG, "Update Feed Posts:, ${result.response.feed}")
                        feedPosts[feedQuery.feed]?.update { Skyline.concat(it, newPosts) }
                    } else {
                        Log.v(TAG,"Feed Posts: ${result.response.feed}")
                        if (feedPosts.containsKey(feedQuery.feed)) {
                            feedPosts[feedQuery.feed]?.emit(newPosts)
                        }
                        feedPosts[feedQuery.feed] = MutableStateFlow(newPosts)
                    }
                }
                //Log.v(TAG, "Feed: ${feedPosts[feedQuery.feed]?.value}")
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