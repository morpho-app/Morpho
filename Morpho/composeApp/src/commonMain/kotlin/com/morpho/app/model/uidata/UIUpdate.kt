package com.morpho.app.model.uidata

import app.cash.paging.PagingData
import com.morpho.app.model.bluesky.AuthorFilter
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.BskyPostThread
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.ui.common.ComposerRole
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.flow.Flow

sealed interface UIUpdate {
    data class OpenComposer(
        val initialContent: BskyPost,
        val role: ComposerRole,
    ): UIUpdate
    data object Empty: UIUpdate
    data object NoOp: UIUpdate
}

sealed interface SearchUpdate: UIUpdate {
    data object Empty: SearchUpdate

    data class Error(val error: String): SearchUpdate

    data class ProfileSearchResults(
        val query: String? = null,
        val term: String? = null,
        val results: Flow<PagingData<MorphoDataItem.ProfileItem>>,
    ): SearchUpdate

    data class ProfileSearchTypeahead(
        val query: String? = null,
        val term: String? = null,
        val results: Flow<PagingData<MorphoDataItem.ProfileItem>>,
    ): SearchUpdate

    data class PostSearchResults(
        val query: String? = null,
        val results: Flow<PagingData<MorphoDataItem.Post>>,
    ): SearchUpdate
}

sealed interface FeedUpdate<Data: MorphoDataItem.FeedItem>: UIUpdate {
    data object Empty: FeedUpdate<MorphoDataItem.FeedItem>

    data class Error(val error: String): FeedUpdate<MorphoDataItem.FeedItem>

    data class Feed(
        val uri: AtUri,
        val feed: Flow<PagingData<MorphoDataItem.FeedItem>>,
    ): FeedUpdate<MorphoDataItem.FeedItem>

    data class Peek(
        val uri: AtUri,
        val post: Flow<MorphoDataItem.FeedItem>,
    ): FeedUpdate<MorphoDataItem.FeedItem>
}

sealed interface AuthorFeedUpdate: UIUpdate {

    data object Empty: AuthorFeedUpdate

    data class Error(val error: String): AuthorFeedUpdate

    data class Feed(
        val actor: AtIdentifier,
        val filter: AuthorFilter,
        val feed: Flow<PagingData<MorphoDataItem.FeedItem>>,
    ): AuthorFeedUpdate

    data class Likes(
        val actor: AtIdentifier,
        val feed: Flow<PagingData<MorphoDataItem.FeedItem>>,
    ): AuthorFeedUpdate

    data class Lists(
        val actor: AtIdentifier,
        val feed: Flow<PagingData<MorphoDataItem.ListInfo>>,
    ): AuthorFeedUpdate

    data class Feeds(
        val actor: AtIdentifier,
        val feed: Flow<PagingData<MorphoDataItem.FeedInfo>>,
    ): AuthorFeedUpdate
}


sealed interface ThreadUpdate: UIUpdate {
    data object Empty: ThreadUpdate

    data class Error(val error: String): ThreadUpdate

    data class Thread(
        val results: BskyPostThread,
    ): ThreadUpdate
}

sealed interface MyProfileUpdate: UIUpdate {
    data object Empty: MyProfileUpdate

    data class Error(val error: String): MyProfileUpdate
    data object Editing: MyProfileUpdate
    data object ExitEditing: MyProfileUpdate
}

sealed interface ActorUpdate: UIUpdate {
    data object Empty : ActorUpdate

    data class Error(val error: String) : ActorUpdate
    data object Followed : ActorUpdate
    data object Unfollowed : ActorUpdate
    data object Muted : ActorUpdate
    data object Unmuted : ActorUpdate
    data object Blocked : ActorUpdate
    data object Unblocked : ActorUpdate
    data object Reported : ActorUpdate
    data object Liked : ActorUpdate
    data object Unliked : ActorUpdate
}