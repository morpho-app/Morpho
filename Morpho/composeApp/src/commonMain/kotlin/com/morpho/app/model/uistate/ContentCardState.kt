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
        init {
            require(
                MorphoData.ProfilePostsUriRegex.matches(uri.atUri) ||
                MorphoData.ProfileRepliesUriRegex.matches(uri.atUri) ||
                MorphoData.ProfileMediaUriRegex.matches(uri.atUri) ||
                MorphoData.ProfileLikesUriRegex.matches(uri.atUri) ||
                MorphoData.ProfileFeedsListUriRegex.matches(uri.atUri) ||
                MorphoData.ProfileUserListsUriRegex.matches(uri.atUri) ||
                MorphoData.ProfileModServiceUriRegex.matches(uri.atUri) ||
                uri == MorphoData.MY_PROFILE_URI
            ) { "Invalid profile feed uri: $uri" }
        }
    }

    @Serializable
    data class FullProfile<T: Profile>(
        val profile: T,
        val postsState: ProfileTimeline<MorphoDataItem.FeedItem>? = null,
        val postRepliesState: ProfileTimeline<MorphoDataItem.FeedItem>? = null,
        val mediaState: ProfileTimeline<MorphoDataItem.FeedItem>? = null,
        val likesState: ProfileTimeline<MorphoDataItem.FeedItem>? = null,
        val listsState: ProfileTimeline<MorphoDataItem.ListInfo>? = null,
        val feedsState: ProfileTimeline<MorphoDataItem.FeedInfo>? = null,
        val modServiceState: ProfileTimeline<MorphoDataItem.ModLabel>? = null,
        override val loadingState: ContentLoadingState = ContentLoadingState.Loading,
        override val hasNewPosts: Boolean = false,
    ) : ContentCardState<MorphoDataItem> {
        override val uri: AtUri =
            when(profile) {
                is DetailedProfile -> MorphoData.profileUri(profile.did)
                is BskyLabelService -> profile.uri
                else -> throw IllegalArgumentException("Invalid profile type: $profile")
            }


        val feedsLoaded: Boolean
            get() = postsState?.loadingState  == ContentLoadingState.Idle &&
                    postRepliesState?.loadingState == ContentLoadingState.Idle &&
                    mediaState?.loadingState == ContentLoadingState.Idle &&
                    ((likesState == null) || (likesState.loadingState == ContentLoadingState.Idle))
    }

    @Serializable
    data class UserList(
        val list: BskyList,
        override val loadingState: ContentLoadingState = ContentLoadingState.Loading,
        override val hasNewPosts: Boolean = false,
    ) : ContentCardState<MorphoDataItem.ListInfo> {
        override val uri: AtUri = list.uri
    }
}