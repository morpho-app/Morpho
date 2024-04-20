package com.morpho.app.screens.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.bsky.actor.GetProfileQuery
import app.bsky.feed.GetFeedGeneratorsQuery
import app.bsky.feed.GetPostThreadQuery
import app.bsky.feed.GetPostThreadResponseThreadUnion
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.stack.mutableStateStackOf
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uidata.BskyDataService
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uidata.FeedInfo
import com.morpho.app.model.uidata.MorphoData
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.ContentLoadingState
import com.morpho.app.screens.base.BaseScreenModel
import com.morpho.app.ui.profile.ProfileTabs
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

@Suppress("UNCHECKED_CAST")
// TODO: Revisit these casts if we can, but they should be safe
open class MainScreenModel: BaseScreenModel() {
    val dataService: BskyDataService by inject()

    val pinnedFeeds = mutableStateListOf<FeedGenerator>()
    val savedFeeds = mutableStateListOf<FeedGenerator>()
    val feedStates = mutableStateListOf<ContentCardState.Skyline<MorphoDataItem.FeedItem>>()
    val threadStates = mutableStateListOf<ContentCardState.PostThread>()
    val profileStates = mutableStateListOf<ContentCardState.FullProfile<Profile>>()
    val profileFeeds = mutableStateListOf<ContentCardState.ProfileTimeline<MorphoDataItem>>()

    val history = mutableStateStackOf<ContentCardMapEntry>()
    var userId: AtIdentifier? by mutableStateOf(null)
        protected set

    companion object {
        val log = logging()
    }
    init {
        val home = MorphoData<MorphoDataItem.FeedItem>("Home", MorphoData.HOME_URI)
        feedStates.add(ContentCardState.Skyline(home))
        screenModelScope.launch {
            if(api.id != null) {
                userId = api.id
            } else if(api.atpUser != null) {
                userId = api.atpUser!!.id
            } else if (api.userService.firstUser() != null) {
                api.atpUser = api.userService.firstUser()
                userId = api.atpUser?.id
            } else {
                userId = api.session.auth().first()?.did
            }
            initFeeds()
        }

    }

    fun getFeedInfo(uri: AtUri) : FeedInfo? {
        when {
            uri == MorphoData.HOME_URI -> return FeedInfo(uri, "Home", "Your home feed", icon = Icons.Default.Home)
            else -> {
                pinnedFeeds.firstOrNull { it.uri == uri }?.let {
                    return FeedInfo(uri, it.displayName, it.description, it.avatar, feed = it)
                }
                savedFeeds.firstOrNull { it.uri == uri }?.let {
                    return FeedInfo(uri, it.displayName, it.description, it.avatar, feed = it)
                }
                return null
            }
        }
    }

    suspend fun initFeeds() = screenModelScope.launch(Dispatchers.IO) {
        val prefs = userId?.let {
            preferences.getPrefsLocal(it).getOrNull()
                ?.let { p -> api.api.getPreferences().getOrNull()?.toPreferences(p) }
        }
        if(prefs?.savedFeeds != null) {
            val mySavedFeeds = prefs.savedFeeds!!
            // Home Timeline, lets make sure this gets filled out well
            launch {
                dataService.getFeedWithMinPosts<MorphoDataItem.FeedItem>(MorphoData.HOME_URI, feedPref = prefs.feedViewPrefs["home"])
                    .onSuccess {
                        val cursor = it.first
                        val posts = it.second
                        val feed = MorphoData("Home", MorphoData.HOME_URI, cursor, posts)
                        // Replace the placeholder feed state with the real one
                        val i = feedStates.indexOfFirst { s -> s.uri == MorphoData.HOME_URI }
                        if(i != -1) feedStates[i] = ContentCardState.Skyline(feed)
                        else feedStates.add(ContentCardState.Skyline(feed))
                    }
            }
            // Pinned feeds (load immediately with posts to minimize lag when switching to them)
            api.api.getFeedGenerators(GetFeedGeneratorsQuery(mySavedFeeds.pinned))
                .onSuccess { r ->
                    pinnedFeeds.addAll(r.feeds.mapImmutable { it.toFeedGenerator()})
                    pinnedFeeds.forEach { feedGen ->
                        launch {
                            dataService.getFeed<MorphoDataItem.FeedItem>(feedGen.uri,
                                                feedPref = prefs.feedViewPrefs[feedGen.uri.atUri])
                                .onSuccess {
                                    val cursor = it.first
                                    val posts = it.second
                                    val feed = MorphoData(feedGen.displayName,
                                                          feedGen.uri, cursor, posts)
                                    feedStates.add(ContentCardState.Skyline(feed))
                                }
                        }
                    }
                }
            // Saved feeds (only get the first post to initialize the feed state)
            api.api.getFeedGenerators(GetFeedGeneratorsQuery(mySavedFeeds.saved))
                .onSuccess { r ->
                    savedFeeds.addAll(r.feeds.mapImmutable { it.toFeedGenerator()})
                    savedFeeds.forEach { feedGen ->
                        // Only load the feed if it's not already pinned
                        if (!pinnedFeeds.any { it.uri == feedGen.uri }) launch {
                            dataService.getFeed<MorphoDataItem.FeedItem>(feedGen.uri, limit = 1,
                                                feedPref = prefs.feedViewPrefs[feedGen.uri.atUri])
                                .onSuccess {
                                    val cursor = it.first
                                    val posts = it.second
                                    val feed = MorphoData(feedGen.displayName,
                                                          feedGen.uri, cursor, posts)
                                    feedStates.add(ContentCardState.Skyline(feed))
                                }
                        }
                    }
                }
        }
    }

    open suspend fun checkNewPosts(entry: ContentCardMapEntry): Boolean = screenModelScope.async {
        return@async dataService.hasNewPosts(entry.uri)
    }.await()

    suspend fun loadThread(state: ContentCardState.PostThread): Deferred<Result<ContentCardState.PostThread>> = screenModelScope.async(Dispatchers.IO) {
        return@async api.api.getPostThread(GetPostThreadQuery(state.uri)).map { response ->
            return@map response.thread.let { thread ->
                when (thread) {
                    is GetPostThreadResponseThreadUnion.BlockedPost -> {
                        ContentCardState.PostThread(state.post,
                                                    MutableStateFlow(null).asStateFlow(),
                                                    ContentLoadingState.Error("Blocked post"))
                    }

                    is GetPostThreadResponseThreadUnion.NotFoundPost -> {
                        ContentCardState.PostThread(state.post,
                                                    MutableStateFlow(null).asStateFlow(),
                                                    ContentLoadingState.Error("Post not found"))
                    }

                    is GetPostThreadResponseThreadUnion.ThreadViewPost -> {
                        ContentCardState.PostThread(thread.value.toPost(),
                                                    MutableStateFlow(thread.value.toThread()).asStateFlow(),
                                                    ContentLoadingState.Idle)
                    }
                }
            }
        }
    }

    private fun <T: MorphoDataItem> indexOf(state: ContentCardState<T>): Int {
        return when(state) {
            is ContentCardState.FullProfile<*> -> profileStates.indexOfFirst { it.uri == state.uri }
            is ContentCardState.PostThread      -> threadStates.indexOfFirst { it.uri == state.uri }
            is ContentCardState.ProfileTimeline<T> -> profileFeeds.indexOfFirst { it.uri == state.uri }
            is ContentCardState.Skyline<*> -> feedStates.indexOfFirst { it.uri == state.uri }
            is ContentCardState.UserList -> TODO()
        }
    }

    suspend fun <T: MorphoDataItem.FeedItem> loadFeed(state: ContentCardState.Skyline<T>): Deferred<Result<ContentCardState.Skyline<T>>> = screenModelScope.async(Dispatchers.IO) {
        dataService.getFeed<T>(state.feed.uri).map {
            val posts = it.second
            val cursor = it.first
            val feed = MorphoData(state.feed.title, state.feed.uri, cursor, posts)
            return@map state.copy(feed = feed, loadingState = ContentLoadingState.Idle)
        }
    }

    suspend fun <T: Profile> loadProfile(state: ContentCardState.FullProfile<T>): Deferred<ContentCardState.FullProfile<T>> = screenModelScope.async(Dispatchers.IO) {
        // Likely one for the tabbed view
        val p = async {
            if (state.postsState == null) dataService.getFeed<MorphoDataItem.FeedItem>(MorphoData.profilePostsUri(state.profile.did))
                .getOrNull() else null }
        val r =  async { if (state.postRepliesState == null) dataService.getFeed<MorphoDataItem.FeedItem>(
            MorphoData.profileRepliesUri(state.profile.did)
        ).getOrNull() else null }
        val m =  async {
            if (state.mediaState == null) dataService.getFeed<MorphoDataItem.FeedItem>(MorphoData.profileMediaUri(state.profile.did))
                .getOrNull() else null }

        val (
            postsResult,
            repliesResult,
            mediaResult)
        = awaitAll(p, r, m)


        val postsFeed = if (postsResult != null) MorphoData(
            "Posts",
            MorphoData.profilePostsUri(state.profile.did),
            postsResult.first,
            postsResult.second
        ) else null
        val repliesFeed = if (repliesResult != null) MorphoData(
            "Posts & Replies",
            MorphoData.profileRepliesUri(state.profile.did),
            repliesResult.first,
            repliesResult.second
        ) else null
        val mediaFeed = if (mediaResult != null) MorphoData(
            "Media",
            MorphoData.profileMediaUri(state.profile.did),
            mediaResult.first,
            mediaResult.second
        ) else null

        val posts = state.postsState
            ?: postsFeed?.let { ContentCardState.ProfileTimeline(state.profile, it) }
        val replies = state.postRepliesState
            ?: repliesFeed?.let { ContentCardState.ProfileTimeline(state.profile, it) }
        val media = state.mediaState
            ?: mediaFeed?.let { ContentCardState.ProfileTimeline(state.profile, it) }
        return@async state.copy(
            postsState = posts,
            postRepliesState = replies,
            mediaState = media,
            loadingState = ContentLoadingState.Idle
        )
    }

    suspend fun <T: MorphoDataItem> loadProfile(state: ContentCardState.ProfileTimeline<T>): Deferred<ContentCardState<out MorphoDataItem>?> = screenModelScope.async(Dispatchers.IO) {
        // Debating if it makes sense to do all the loading here
        // This one likely won't be used much in the tabbed view
        val profile: DetailedProfile? = if(state.profile is DetailedProfile) state.profile
        else {
            api.api.getProfile(GetProfileQuery(state.profile.did)).getOrNull()?.toProfile()
        }
        var postsState: ContentCardState.ProfileTimeline<MorphoDataItem.FeedItem>? = null
        var repliesState: ContentCardState.ProfileTimeline<MorphoDataItem.FeedItem>? = null
        var mediaState: ContentCardState.ProfileTimeline<MorphoDataItem.FeedItem>? = null
        var likesState: ContentCardState.ProfileTimeline<MorphoDataItem.FeedItem>? = null
        val feedType = when {
            MorphoData.ProfilePostsUriRegex.matches(state.uri.atUri) -> {
                ProfileTabs.Posts
            }
            MorphoData.ProfileRepliesUriRegex.matches(state.uri.atUri) -> {
                ProfileTabs.PostsReplies
            }
            MorphoData.ProfileMediaUriRegex.matches(state.uri.atUri) -> {
                ProfileTabs.Media
            }
            MorphoData.ProfileLikesUriRegex.matches(state.uri.atUri) -> {
                ProfileTabs.Likes
            }
            state.uri == MorphoData.MY_PROFILE_URI -> {
                null
            }
            else -> {
                return@async state
            }
        }
        when (feedType) {
            ProfileTabs.Posts -> { postsState = state as ContentCardState.ProfileTimeline<MorphoDataItem.FeedItem> }
            ProfileTabs.PostsReplies -> { repliesState = state as ContentCardState.ProfileTimeline<MorphoDataItem.FeedItem>}
            ProfileTabs.Media -> { mediaState = state as ContentCardState.ProfileTimeline<MorphoDataItem.FeedItem>}
            ProfileTabs.Feeds -> TODO()
            ProfileTabs.Lists -> TODO()
            ProfileTabs.Likes -> { likesState = state as ContentCardState.ProfileTimeline<MorphoDataItem.FeedItem>}
            ProfileTabs.Labeler -> TODO()
            null -> {}

        }
        if (postsState == null) {
            dataService.getFeed<MorphoDataItem.FeedItem>(MorphoData.profilePostsUri(state.profile.did)).onSuccess {
                val posts = it.second
                val cursor = it.first
                val feed = MorphoData("Posts", MorphoData.profilePostsUri(state.profile.did), cursor, posts)
                postsState = ContentCardState.ProfileTimeline(state.profile, feed, ContentLoadingState.Idle)
            }
        }
        if (repliesState == null) {
            dataService.getFeed<MorphoDataItem.FeedItem>(MorphoData.profileRepliesUri(state.profile.did)).onSuccess {
                val posts = it.second
                val cursor = it.first
                val feed = MorphoData("Posts & Replies", MorphoData.profileRepliesUri(state.profile.did), cursor, posts)
                repliesState = ContentCardState.ProfileTimeline(state.profile, feed, ContentLoadingState.Idle)
            }
        }
        if (mediaState == null) {
            dataService.getFeed<MorphoDataItem.FeedItem>(MorphoData.profileMediaUri(state.profile.did)).onSuccess {
                val posts = it.second
                val cursor = it.first
                val feed = MorphoData("Media", MorphoData.profileMediaUri(state.profile.did), cursor, posts)
                mediaState = ContentCardState.ProfileTimeline(state.profile, feed, ContentLoadingState.Idle)
            }
        }
        return@async profile?.let {
            ContentCardState.FullProfile(
                profile = it,
                postsState = postsState,
                postRepliesState = repliesState,
                mediaState = mediaState,
                likesState = likesState,
                loadingState = ContentLoadingState.Idle
            )
        }
    }

    suspend fun <T: MorphoDataItem> loadState(state: ContentCardState<T>): Deferred<Result<ContentCardState<T>>> = screenModelScope.async {

        val newState: ContentCardState<out MorphoDataItem> =  when(state) {
            is ContentCardState.Skyline<*> -> {
                val cursor = dataService.updateFeed<T>(state.uri)
                    .getOrElse { return@async Result.failure(it) }
                val newFeed = dataService.postLists[state.uri] ?: return@async Result.failure(Exception("Feed ${state.uri} not found"))
                ContentCardState.Skyline<MorphoDataItem.FeedItem>(
                    feed = MorphoData(state.feed.title, state.feed.uri, cursor, newFeed as StateFlow<ImmutableList<MorphoDataItem.FeedItem>>),
                    loadingState = ContentLoadingState.Idle
                )
            }
            is ContentCardState.FullProfile<*> -> {
                loadProfile(state).await()
            }
            is ContentCardState.PostThread      -> {
                loadThread(state).await().getOrElse { return@async Result.failure(it) }
            }
            is ContentCardState.ProfileTimeline<T> -> {
                val cursor = dataService.updateFeed<T>(state.uri)
                    .getOrElse { return@async Result.failure(it) }
                val newFeed = dataService.postLists[state.uri] ?: return@async Result.failure(Exception("Feed ${state.uri} not found"))
                state.copy(feed = state.feed.copy(list = newFeed as StateFlow<ImmutableList<T>>, cursor = cursor), loadingState = ContentLoadingState.Idle)
            }

            is ContentCardState.UserList -> TODO()
        }
        return@async Result.success(newState) as Result<ContentCardState<T>>
    }
    suspend fun loadState(entry: ContentCardMapEntry): Deferred<Result<ContentCardState<out MorphoDataItem>>> = screenModelScope.async {
        val state = feedStates.firstOrNull { it.uri == entry.uri }
            ?: threadStates.firstOrNull { it.uri == entry.uri }
            ?: profileStates.firstOrNull { it.uri == entry.uri }
            ?: profileFeeds.firstOrNull { it.uri == entry.uri }
            ?: return@async Result.failure(Exception("State not found for ${entry.uri}"))
        return@async loadState(state).await()
    }
    fun <T: MorphoDataItem> unloadContent(state: ContentCardState<T>): MorphoDataFeed? {

        when(state) {
            is ContentCardState.Skyline<*> -> {
                feedStates.removeAll { it.uri == state.uri }
            }
            is ContentCardState.PostThread -> {
                threadStates.removeAll { it.uri == state.uri }
            }
            is ContentCardState.FullProfile<*> -> {
                profileStates.removeAll { it.uri == state.uri }
                state.postsState?.let { unloadContent(it) }
                state.postRepliesState?.let { unloadContent(it) }
                state.mediaState?.let { unloadContent(it) }
                state.likesState?.let { unloadContent(it) }
            }
            is ContentCardState.ProfileTimeline<T> -> {
                profileFeeds.removeAll { it.uri == state.uri }
            }

            is ContentCardState.UserList -> TODO()
        }
        return dataService.removeFeed(state.uri)
    }

    protected open fun unloadContent(entry: ContentCardMapEntry): MorphoDataFeed? {
        return unloadContent(entry.uri)
    }

    protected fun unloadContent(uri: AtUri): MorphoDataFeed? {
        val state = feedStates.firstOrNull { it.uri == uri }
            ?: threadStates.firstOrNull { it.uri == uri }
            ?: profileStates.firstOrNull { it.uri == uri }
            ?: profileFeeds.firstOrNull { it.uri == uri }
            ?: return null
        return unloadContent(state)
    }
}