package com.morpho.app.model.uidata

import app.bsky.feed.GetActorFeedsQuery
import app.bsky.graph.GetListsQuery
import app.cash.paging.Pager
import app.cash.paging.cachedIn
import com.morpho.app.data.MorphoAgent
import com.morpho.app.data.MorphoDataSource
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.bluesky.toFeedGenerator
import com.morpho.app.model.bluesky.toList
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.Cursor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class Presenter<E: Event>: KoinComponent {
    val agent: MorphoAgent by inject()
    val presenterScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    abstract fun <E: Event> produceUpdates(events: Flow<E>): Flow<UIUpdate>
}



abstract class PagedPresenter<Data:MorphoDataItem, E: Event>: Presenter<E>() {
    abstract var pager: Pager<Cursor, Data>
}

class UserListPresenter(
    val actor: AtIdentifier,
): PagedPresenter<MorphoDataItem.ListInfo, ListEvent>() {
    override var pager: Pager<Cursor, MorphoDataItem.ListInfo> = run {
        val pagingConfig = MorphoDataSource.defaultConfig
        Pager(pagingConfig) {
            UserListFeedSource(actor)
        }
    }

    override fun <E : Event> produceUpdates(events: Flow<E>): Flow<UIUpdate> = events.map { event ->
        when(event) {
            is FeedEvent.LoadLists -> AuthorFeedUpdate.Lists(actor, pager.flow.cachedIn(presenterScope))
            else -> AuthorFeedUpdate.Error("Unknown event type: $event")
        }
    }

}

class UserListFeedSource(
    val actor: AtIdentifier,
): MorphoDataSource<MorphoDataItem.ListInfo>() {

    override suspend fun load(params: LoadParams<Cursor>): LoadResult<Cursor, MorphoDataItem.ListInfo> {
        try {
            val limit = params.loadSize
            val loadCursor = when(params) {
                is LoadParams.Append -> params.key
                is LoadParams.Prepend -> Cursor.Empty
                is LoadParams.Refresh -> Cursor.Empty
            }
            return agent.api.getLists(GetListsQuery(actor, limit.toLong(), loadCursor.value)).map { response ->
                val newCursor = Cursor(response.cursor)
                val items = response.lists
                    .map { MorphoDataItem.ListInfo(it.toList()) }
                LoadResult.Page(
                    data = items,
                    prevKey = when(params) {
                        is LoadParams.Append -> loadCursor
                        is LoadParams.Prepend -> Cursor.Empty
                        is LoadParams.Refresh -> Cursor.Empty
                    },
                    nextKey = newCursor,
                )
            }.onFailure {
                return LoadResult.Error(it)
            }.getOrDefault(LoadResult.Error(Exception("Load failed")))
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}

class UserFeedsPresenter(
    val actor: AtIdentifier,
): PagedPresenter<MorphoDataItem.FeedInfo, ListEvent>() {
    override var pager: Pager<Cursor, MorphoDataItem.FeedInfo> = run {
        val pagingConfig = MorphoDataSource.defaultConfig
        Pager(pagingConfig) {
            UserFeedsFeedSource(actor)
        }
    }

    override fun <E : Event> produceUpdates(events: Flow<E>): Flow<UIUpdate> = events.map { event ->
        when(event) {
            is FeedEvent.LoadLists -> AuthorFeedUpdate.Feeds(actor, pager.flow.cachedIn(presenterScope))
            else -> AuthorFeedUpdate.Error("Unknown event type: $event")
        }
    }

}

class UserFeedsFeedSource(
    val actor: AtIdentifier,
): MorphoDataSource<MorphoDataItem.FeedInfo>() {

    override suspend fun load(params: LoadParams<Cursor>): LoadResult<Cursor, MorphoDataItem.FeedInfo> {
        try {
            val limit = params.loadSize
            val loadCursor = when(params) {
                is LoadParams.Append -> params.key
                is LoadParams.Prepend -> Cursor.Empty
                is LoadParams.Refresh -> Cursor.Empty
            }
            return agent.api
                .getActorFeeds(GetActorFeedsQuery(actor, limit.toLong(), loadCursor.value))
                .map { response ->
                    val newCursor = Cursor(response.cursor)
                    val items = response.feeds
                        .map { MorphoDataItem.FeedInfo(it.toFeedGenerator()) }
                    LoadResult.Page(
                        data = items,
                        prevKey = when(params) {
                            is LoadParams.Append -> loadCursor
                            is LoadParams.Prepend -> Cursor.Empty
                            is LoadParams.Refresh -> Cursor.Empty
                        },
                        nextKey = newCursor,
                    )
                }.onFailure {
                    return LoadResult.Error(it)
                }.getOrDefault(LoadResult.Error(Exception("Load failed")))
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}