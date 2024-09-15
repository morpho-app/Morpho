package com.morpho.app.model.uidata

import app.cash.paging.PagingData
import com.morpho.app.model.bluesky.*
import com.morpho.app.ui.common.ComposerRole
import com.morpho.butterfly.AtIdentifier
import kotlinx.coroutines.flow.Flow

sealed interface UIUpdate {
    data class OpenComposer(
        val initialContent: BskyPost,
        val role: ComposerRole,
    ): UIUpdate
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

sealed interface FeedUpdate: UIUpdate {
    data object Empty: FeedUpdate

    data class Error(val error: String): FeedUpdate

    data class Feed<Data: MorphoDataItem.FeedItem>(
        val info: FeedSourceInfo,
        val feed: Flow<PagingData<Data>>,
    ): FeedUpdate

    data class Peek<Data: MorphoDataItem.FeedItem>(
        val info: FeedSourceInfo,
        val post: Flow<Data>,
    ): FeedUpdate
}

sealed interface AuthorFeedUpdate: UIUpdate {

    data object Empty: AuthorFeedUpdate

    data class Error(val error: String): AuthorFeedUpdate

    data class Feed<Data: MorphoDataItem.FeedItem>(
        val actor: AtIdentifier,
        val filter: AuthorFilter,
        val feed: Flow<PagingData<Data>>,
    ): AuthorFeedUpdate

    data class Likes<Data: MorphoDataItem.FeedItem>(
        val actor: AtIdentifier,
        val feed: Flow<PagingData<Data>>,
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
        val results: Flow<BskyPostThread>,
    ): ThreadUpdate
}