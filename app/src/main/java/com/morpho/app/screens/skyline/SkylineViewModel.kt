package com.morpho.app.screens.skyline

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
import app.bsky.feed.GetFeedQuery
import app.bsky.feed.GetTimelineQuery
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
import com.morpho.butterfly.AtIdentifier
import com.morpho.app.model.BskyFeedPref
import com.morpho.app.butterfly
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordUnion
import com.morpho.app.base.BaseViewModel
import com.morpho.app.model.BasicProfile
import com.morpho.app.model.Skyline
import com.morpho.app.model.Skyline.Companion.filterByPrefs
import com.morpho.app.model.TunerFunction
import com.morpho.app.model.toBskyPostList
import com.morpho.app.model.tune
import com.morpho.app.screens.destinations.PostThreadScreenDestination
import com.morpho.app.screens.destinations.ProfileScreenDestination
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
    val apiProvider = app.butterfly
    var state by mutableStateOf(SkylineState())
        private set

    private val _skylinePosts = MutableStateFlow(Skyline(cursor = null))
    val skylinePosts: StateFlow<Skyline> = _skylinePosts.asStateFlow()
    val feedPosts = mutableStateMapOf(Pair(AtUri(""),MutableStateFlow( Skyline(cursor = null))))
    var pinnedFeeds = mutableStateListOf<FeedTab>()

    fun hasPosts() = viewModelScope.async {
        if (state.hasNewPosts) return@async
        launch(Dispatchers.IO) {
            apiProvider.api.getTimeline(GetTimelineQuery(limit = 1, cursor = null))
                .onSuccess { response ->
                    if (response.feed.isNotEmpty()) {
                        val cid = response.feed.first().post.cid
                        if (!skylinePosts.value.contains(cid)) {
                            _skylinePosts.update { it.copy(hasNewPosts = true) }
                            state = state.copy(hasNewPosts = true)
                            return@launch
                        }
                    }
                }
        }.join()
        pinnedFeeds.fastMap { tab ->
            launch(Dispatchers.IO) {
                apiProvider.api.getFeed(GetFeedQuery(limit = 1, cursor = null, feed = tab.uri))
                    .onSuccess { response ->
                        if (response.feed.isNotEmpty()) {
                            val cid = response.feed.first().post.cid
                            if (feedPosts[tab.uri]?.value?.contains(cid) != true) {
                                state = state.copy(hasNewPosts = true)
                                feedPosts[tab.uri]?.update { it.copy(hasNewPosts = true) }
                                return@launch
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
        apiProvider.api.getTimeline(GetTimelineQuery(limit = limit, cursor = cursor))
            .onFailure {
                Log.e(TAG, "Load Err $it")
            }
            .onSuccess {
                val tuners = mutableListOf<TunerFunction>()
                tuners.add { posts -> filterByPrefs(posts, prefs, follows.fastMap { it.did }) }

                val newPosts = it.feed.toBskyPostList().tune(tuners)


                if (cursor != null) {
                    Log.v(TAG, "UpdatePosts:, ${it.feed}")
                    _skylinePosts.update { skyline -> Skyline.concat(skyline, Skyline.collectThreads(apiProvider,it.cursor,newPosts).await()) }//Skyline.collectThreads(apiProvider, result.response.cursor, newPosts).await()) }
                    //_skylinePosts.update { Skyline.collectThreads(apiProvider,result.response.cursor,newPosts).await() }
                } else {
                    Log.v(TAG,"Posts: ${it.feed}")
                    //_skylinePosts.update { Skyline.from(newPosts, result.response.cursor) }
                    _skylinePosts.emit(Skyline.collectThreads(apiProvider,it.cursor,newPosts).await())
                }
            }
    }

    fun getSkyline(
        feedQuery: GetFeedQuery,
        cursor: String? = null,
    ) = viewModelScope.launch(Dispatchers.IO) {
        run {
            if(cursor == null) apiProvider.api.getFeed(feedQuery) else {
                apiProvider.api.getFeed(feedQuery.copy(cursor = cursor))
            }
        }.onFailure {
            Log.e(TAG, "Feed Load Err $it")
        }.onSuccess {
            val newPosts = Skyline.from(it.feed.toBskyPostList(), it.cursor)
            if (it.feed.isNotEmpty() ){
                if (cursor != null || feedQuery.cursor != null) {
                    Log.v(TAG, "Update Feed Posts:, ${it.feed}")
                    feedPosts[feedQuery.feed]?.update { Skyline.concat(it, newPosts) }
                } else {
                    Log.v(TAG,"Feed Posts: ${it.feed}")
                    if (feedPosts.containsKey(feedQuery.feed)) {
                        feedPosts[feedQuery.feed]?.emit(newPosts)
                    }
                    feedPosts[feedQuery.feed] = MutableStateFlow(newPosts)
                }
            }
            //Log.v(TAG, "Feed: ${feedPosts[feedQuery.feed]?.value}")
        }
    }

    fun onItemClicked(uri: AtUri, navigator: DestinationsNavigator) {
        navigator.navigate(PostThreadScreenDestination(uri))
    }

    fun onProfileClicked(actor: AtIdentifier, navigator: DestinationsNavigator) {
        navigator.navigate(ProfileScreenDestination(actor))
    }

}