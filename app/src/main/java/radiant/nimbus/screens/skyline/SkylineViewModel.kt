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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.BskyFeedPref
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.base.BaseViewModel
import radiant.nimbus.model.BasicProfile
import radiant.nimbus.model.Skyline
import radiant.nimbus.model.Skyline.Companion.filterByPrefs
import radiant.nimbus.model.TunerFunction
import radiant.nimbus.model.toBskyPostList
import radiant.nimbus.model.tune
import javax.inject.Inject

data class SkylineState(
    val isLoading : Boolean = false,
    val feedUri: AtUri? = null
)

private const val TAG = "Skyline"

@HiltViewModel
class SkylineViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(SkylineState())
        private set

    private val _skylinePosts = MutableStateFlow(Skyline(emptyList(), null))
    val skylinePosts: StateFlow<Skyline> = _skylinePosts.asStateFlow()
    val feedPosts = mutableStateMapOf(Pair(AtUri(""),MutableStateFlow( Skyline(emptyList(), null))))
    var pinnedFeeds = mutableStateListOf<FeedTab>()

    fun createRecord(
        record: RecordUnion,
        apiProvider: ApiProvider,
    ) = CoroutineScope(Dispatchers.Default).launch {
        apiProvider.createRecord(record)
    }

    fun getSkyline(
        apiProvider: ApiProvider,
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
                    _skylinePosts.update { Skyline.collectThreads(apiProvider,result.response.cursor,newPosts).await() }
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
                Log.e(TAG, "Feed Load Err $result")
            }

            is AtpResponse.Success -> {
                val newPosts = Skyline.from(result.response.feed.toBskyPostList(), result.response.cursor)
                if (result.response.feed.isNotEmpty() ){
                    if (cursor != null || feedQuery.cursor != null) {
                        Log.v(TAG, "Update Feed Posts:, ${result.response.feed}")
                        feedPosts[feedQuery.feed]?.update { Skyline.concat(it, newPosts) }
                    } else {
                        if(result.response.feed.first().post.indexedAt.toEpochMilliseconds() >
                                (feedPosts[feedQuery.feed]?.value?.posts?.first()?.post?.indexedAt?.instant?.toEpochMilliseconds() ?: 0)
                        ) {
                            Log.v(TAG, "Prepend Feed Posts:, ${result.response.feed}")
                            if(feedPosts.containsKey(feedQuery.feed)) {
                                feedPosts[feedQuery.feed]!!.update { Skyline.concat(newPosts, it, result.response.cursor) }
                            } else {
                                feedPosts[feedQuery.feed] = MutableStateFlow(newPosts)
                            }
                        } else {
                            Log.v(TAG,"Feed Posts: ${result.response.feed}")
                            feedPosts[feedQuery.feed] = MutableStateFlow(newPosts)
                        }
                    }
                }
                //Log.v(TAG, "Feed: ${feedPosts[feedQuery.feed]?.value}")
            }
        }
    }

}