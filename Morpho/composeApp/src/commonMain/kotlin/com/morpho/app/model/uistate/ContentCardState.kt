package com.morpho.app.model.uistate

import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uidata.*
import com.morpho.app.util.MutableSharedFlowSerializer
import com.morpho.app.util.MutableStateFlowSerializer
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable


@Suppress("unused")
@Serializable
sealed interface ContentCardState<E: Event> {
    val uri: AtUri
    @Serializable(with = MutableSharedFlowSerializer::class)
    val events: MutableSharedFlow<E>
    @Serializable(with = MutableStateFlowSerializer::class)
    val updates: MutableStateFlow<UIUpdate>

    @Serializable
    data class Skyline(
        override val uri: AtUri,
        override val events: MutableSharedFlow<FeedEvent> = MutableSharedFlow(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST),
        override val updates: MutableStateFlow<UIUpdate> = MutableStateFlow(FeedUpdate.Empty),
    ) : ContentCardState<FeedEvent>

    @Serializable
    data class PostThread(
        val post: BskyPost,
        override val events: MutableSharedFlow<ThreadEvent> = MutableSharedFlow(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST),
        override val updates: MutableStateFlow<UIUpdate> = MutableStateFlow(UIUpdate.Empty),
        ): ContentCardState<ThreadEvent> {
        override val uri: AtUri = post.uri
        init {
            require(post.uri.atUri.contains("app.bsky.feed.post")) {
                "Invalid post uri: $uri"
            }
        }
    }

    @Serializable
    data class ProfileTimeline(
        val profile: DetailedProfile,
        val filter: AuthorFilter? = AuthorFilter.PostsWithReplies,
        override val events: MutableSharedFlow<FeedEvent> = MutableSharedFlow(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST),
        override val updates: MutableStateFlow<UIUpdate> = MutableStateFlow(FeedUpdate.Empty),
    ) : ContentCardState<FeedEvent> {
        override val uri: AtUri = when(filter) {
            AuthorFilter.PostsWithReplies -> AtUri.profileRepliesUri(profile.did)
            AuthorFilter.PostsNoReplies -> AtUri.profilePostsUri(profile.did)
            AuthorFilter.PostsAuthorThreads -> AtUri.profileRepliesUri(profile.did)
            AuthorFilter.PostsWithMedia -> AtUri.profileMediaUri(profile.did)
            null -> AtUri.profileLikesUri(profile.did)
        }
    }

    data class ProfileList(
        val profile: Profile,
        val listsOrFeeds: ListsOrFeeds = ListsOrFeeds.Lists,
        override val events: MutableSharedFlow<ListEvent> = MutableSharedFlow(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST),
        override val updates: MutableStateFlow<UIUpdate> = MutableStateFlow(UIUpdate.Empty),
    ): ContentCardState<ListEvent> {
        override val uri: AtUri = when(listsOrFeeds) {
            ListsOrFeeds.Lists -> AtUri.profileUserListsUri(profile.did)
            ListsOrFeeds.Feeds -> AtUri.profileFeedsListUri(profile.did)
        }
    }

    data class ProfileLabeler(
        val profile: BskyLabelService,
        override val uri: AtUri,
        override val events: MutableSharedFlow<LabelerEvent> = MutableSharedFlow(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST),
        override val updates: MutableStateFlow<UIUpdate> = MutableStateFlow(UIUpdate.Empty),
    ): ContentCardState<LabelerEvent>

    data class FullProfile(
        val profile: DetailedProfile,
        val lists: ProfileList? = null,
        val feeds: ProfileList? = null,
        val labeler: ProfileLabeler? = null,
        val posts: ProfileTimeline = ProfileTimeline(profile, AuthorFilter.PostsNoReplies),
        val postReplies: ProfileTimeline = ProfileTimeline(profile, AuthorFilter.PostsWithReplies),
        val media: ProfileTimeline = ProfileTimeline(profile, AuthorFilter.PostsWithMedia),
        override val events: MutableSharedFlow<Event> = MutableSharedFlow(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST),
        override val updates: MutableStateFlow<UIUpdate> = MutableStateFlow(UIUpdate.Empty),
    ) : ContentCardState<Event> {
        override val uri: AtUri = AtUri.profileUri(profile.did)
    }

    data class MyProfile(
        val profile: DetailedProfile,
        val lists: ProfileList? = null,
        val feeds: ProfileList? = null,
        val labeler: ProfileLabeler? = null,
        val posts: ProfileTimeline = ProfileTimeline(profile, AuthorFilter.PostsNoReplies),
        val postReplies: ProfileTimeline = ProfileTimeline(profile, AuthorFilter.PostsWithReplies),
        val media: ProfileTimeline = ProfileTimeline(profile, AuthorFilter.PostsWithMedia),
        val likes: ProfileTimeline = ProfileTimeline(profile, null),
        override val events: MutableSharedFlow<Event> = MutableSharedFlow(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST),
        override val updates: MutableStateFlow<UIUpdate> = MutableStateFlow(UIUpdate.Empty),
    ) : ContentCardState<Event> {
        override val uri: AtUri = AtUri.profileUri(profile.did)
    }

    @Serializable
    data class UserListPage<E: ListPageEvent>(
        val list: BskyList,
        override val events: MutableSharedFlow<E> = MutableSharedFlow(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST),
        override val updates: MutableStateFlow<UIUpdate> = MutableStateFlow(UIUpdate.Empty),
    ) : ContentCardState<E> {
        override val uri: AtUri = list.uri
    }

    @Serializable
    data class FeedPage<E: ListPageEvent>(
        val list: BskyList,
        override val events: MutableSharedFlow<E> = MutableSharedFlow(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST),
        override val updates: MutableStateFlow<UIUpdate> = MutableStateFlow(UIUpdate.Empty),
    ) : ContentCardState<E> {
        override val uri: AtUri = list.uri
    }
}

enum class ListsOrFeeds {
    Lists,
    Feeds
}