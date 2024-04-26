package com.morpho.app.model.uistate

import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uidata.MorphoData
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Suppress("unused")
@Serializable
sealed interface ContentCardState<T: MorphoDataItem> {
    val uri: AtUri
    val feed: MorphoData<T>
    val hasNewPosts: Boolean
    val loadingState: ContentLoadingState

    @Serializable
    data class Skyline<T: MorphoDataItem.FeedItem>(
        override val feed: MorphoData<T>,
        override val loadingState: ContentLoadingState = ContentLoadingState.Loading,
        override val hasNewPosts: Boolean = false,
    ) : ContentCardState<T>, SkylineContentState<T> {
        override val uri: AtUri = feed.uri
    }

    @Serializable
    data class PostThread(
        val post: BskyPost,
        val thread: StateFlow<BskyPostThread?> = MutableStateFlow(null).asStateFlow(),
        override val loadingState: ContentLoadingState = ContentLoadingState.Loading,
        override val hasNewPosts: Boolean = false,

        ): ContentCardState<MorphoDataItem.Thread>, PostThreadContentState {

        override val uri: AtUri = post.uri
        override val feed: MorphoData<MorphoDataItem.Thread> = MorphoData(
            uri = post.uri,
            title = "${post.author.displayName}'s Thread",
        )

        init {
            require(post.uri.atUri.contains("app.bsky.feed.post")) {
                "Invalid post uri: $uri"
            }
        }
    }

    @Serializable
    data class ProfileTimeline<T : MorphoDataItem>(
        val profile: Profile,
        override val feed: MorphoData<T>,
        override val loadingState: ContentLoadingState = ContentLoadingState.Loading,
        override val hasNewPosts: Boolean = false,
    ) : ContentCardState<T>, SkylineContentState<T> {
        override val uri: AtUri = feed.uri
        /*init {
            require(
                AtUri.ProfilePostsUriRegex.matches(uri.atUri) ||
                    AtUri.ProfileRepliesUriRegex.matches(uri.atUri) ||
                    AtUri.ProfileMediaUriRegex.matches(uri.atUri) ||
                    AtUri.ProfileLikesUriRegex.matches(uri.atUri) ||
                    AtUri.ProfileFeedsListUriRegex.matches(uri.atUri) ||
                    AtUri.ProfileUserListsUriRegex.matches(uri.atUri) ||
                    AtUri.ProfileModServiceUriRegex.matches(uri.atUri) ||
                uri == AtUri.MY_PROFILE_URI
            ) { "Invalid profile feed uri: $uri" }
        }*/
    }

    @Serializable
    data class FullProfile<T: Profile>(
        val profile: T,
        val postsState: StateFlow<ProfileTimeline<MorphoDataItem.FeedItem>?> = MutableStateFlow(null).asStateFlow(),
        val postRepliesState: StateFlow<ProfileTimeline<MorphoDataItem.FeedItem>?> = MutableStateFlow(null).asStateFlow(),
        val mediaState: StateFlow<ProfileTimeline<MorphoDataItem.FeedItem>?> = MutableStateFlow(null).asStateFlow(),
        val likesState: StateFlow<ProfileTimeline<MorphoDataItem.FeedItem>?> = MutableStateFlow(null).asStateFlow(),
        val listsState: StateFlow<ProfileTimeline<MorphoDataItem.ListInfo>?> = MutableStateFlow(null).asStateFlow(),
        val feedsState: StateFlow<ProfileTimeline<MorphoDataItem.FeedInfo>?> = MutableStateFlow(null).asStateFlow(),
        val modServiceState: StateFlow<ProfileTimeline<MorphoDataItem.LabelService>?> = MutableStateFlow(null).asStateFlow(),
        override val loadingState: ContentLoadingState = ContentLoadingState.Loading,
        override val hasNewPosts: Boolean = false,
    ) : ContentCardState<MorphoDataItem> {
        override val uri: AtUri =
            when(profile) {
                is DetailedProfile -> AtUri.profileUri(profile.did)
                is BskyLabelService -> profile.uri
                else -> throw IllegalArgumentException("Invalid profile type: $profile")
            }
        override val feed: MorphoData<MorphoDataItem> = MorphoData(
            uri = uri,
            title = profile.displayName.orEmpty(),
        )


        val feedsLoaded: Boolean
            get() = postsState.value?.loadingState  == ContentLoadingState.Idle &&
                    postRepliesState.value?.loadingState == ContentLoadingState.Idle &&
                    mediaState.value?.loadingState == ContentLoadingState.Idle &&
                    ((likesState.value == null) || (likesState.value?.loadingState == ContentLoadingState.Idle))
    }

    @Serializable
    data class UserList(
        val list: BskyList,
        override val loadingState: ContentLoadingState = ContentLoadingState.Loading,
        override val hasNewPosts: Boolean = false,
    ) : ContentCardState<MorphoDataItem.ListInfo> {
        override val uri: AtUri = list.uri
        override val feed: MorphoData<MorphoDataItem.ListInfo> = MorphoData(
            uri = list.uri,
            title = list.name,
        )
    }
}