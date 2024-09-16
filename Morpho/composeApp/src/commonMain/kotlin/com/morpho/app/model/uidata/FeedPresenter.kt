package com.morpho.app.model.uidata

import app.bsky.feed.GetAuthorFeedFilter
import app.bsky.feed.GetFeedQuery
import app.bsky.feed.GetListFeedQuery
import app.bsky.graph.GetListQuery
import app.cash.paging.Pager
import app.cash.paging.cachedIn
import com.morpho.app.data.FeedTuner
import com.morpho.app.data.MorphoDataSource
import com.morpho.app.data.MorphoFeedSource
import com.morpho.app.model.bluesky.*
import com.morpho.butterfly.ButterflyAgent
import com.morpho.butterfly.Cursor
import com.morpho.butterfly.FeedRequest
import com.morpho.butterfly.PagedResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class FeedPresenter<Data: MorphoDataItem.FeedItem, E: FeedEvent>(
    descriptor: FeedDescriptor? = null,
): Presenter<Data, E>() {

    private var dataSource: MorphoFeedSource<Data> =
        descriptor?.getDataSource(agent) ?: getTimelineDataSource(agent)

    override var pager: Pager<Cursor, Data> = run {
        val pagingConfig = MorphoDataSource.defaultConfig
        Pager(pagingConfig) {
            dataSource
        }
    }

    private fun switchPager(newDataSource: MorphoFeedSource<Data>) {
        dataSource = newDataSource
        pager = Pager(MorphoDataSource.defaultConfig) {
            dataSource
        }
    }

    override fun produceUpdates(events: Flow<E>): Flow<UIUpdate> = events.map { event ->
        when(event) {
            is FeedEvent.ComposePost -> UIUpdate.OpenComposer(event.post, event.role)
            is FeedEvent.Load -> {
                switchPager(event.descriptor.getDataSource(agent))
                when(event.descriptor) {
                    is FeedDescriptor.Author -> AuthorFeedUpdate.Feed(
                        event.descriptor.did, event.descriptor.filter, pager.flow.cachedIn(presenterScope))
                    is FeedDescriptor.FeedGen -> {
                        val info = agent.api
                            .getList(GetListQuery(event.descriptor.uri, 1))
                            .map { it.list.hydrateList() }
                        if(info.isSuccess) {
                            switchPager(info.getOrThrow().getDataSource(agent))
                            FeedUpdate.Feed(info.getOrThrow(), pager.flow.cachedIn(presenterScope))
                        } else {
                            FeedUpdate.Error(info.exceptionOrNull()?.message ?:
                                "Failed to load saved feed: ${event.descriptor}, error: $info")
                        }
                    }
                    FeedDescriptor.Home -> FeedUpdate.Feed(
                        FeedSourceInfo.Home, pager.flow.cachedIn(presenterScope))
                    is FeedDescriptor.Likes -> AuthorFeedUpdate.Likes(
                        event.descriptor.did, pager.flow.cachedIn(presenterScope))
                    is FeedDescriptor.List -> FeedUpdate.Error(
                        "Internal error: LoadLists should not be sent to this presenter")
                }
            }
            is FeedEvent.LoadLists -> FeedUpdate.Error(
                "Internal error: LoadLists should not be sent to this presenter")
            is FeedEvent.LoadHydrated -> {
                switchPager(event.info.getDataSource(agent))
                FeedUpdate.Feed(event.info, pager.flow.cachedIn(presenterScope))
            }
            is FeedEvent.LoadSaved -> {
                val info = event.info.toFeedSourceInfo(agent)
                if(info.isSuccess) {
                    switchPager(info.getOrThrow().getDataSource(agent))
                    FeedUpdate.Feed(info.getOrThrow(), pager.flow.cachedIn(presenterScope))
                } else {
                    FeedUpdate.Error(info.exceptionOrNull()?.message ?:
                        "Failed to load saved feed: ${event.info}")
                }
            }
            is FeedEvent.Peek -> FeedUpdate.Peek(event.info, dataSource.updates())
            else -> FeedUpdate.Error("Unknown event type: $event")
        }
    }
}

fun <Data: MorphoDataItem.FeedItem> FeedSourceInfo.getDataSource(
    agent: ButterflyAgent,
): MorphoFeedSource<Data> {
    return when(this) {
        is FeedSourceInfo.FeedInfo -> getFeedDataSource(this.feedDescriptor as FeedDescriptor.FeedGen, agent)
        is FeedSourceInfo.ListInfo -> getListDataSource(this.feedDescriptor as FeedDescriptor.List, agent)
        else -> getTimelineDataSource(agent)
    }
}

fun <Data: MorphoDataItem.FeedItem> FeedDescriptor.getDataSource(
    agent: ButterflyAgent,
): MorphoFeedSource<Data> {
    return when(this) {
        is FeedDescriptor.FeedGen -> getFeedDataSource(this, agent)
        is FeedDescriptor.List -> getListDataSource(this, agent)
        is FeedDescriptor.Home -> getTimelineDataSource(agent)
        is FeedDescriptor.Author -> getAuthorFeedDataSource(this, agent)
        is FeedDescriptor.Likes -> getLikesDataSource(this, agent)
    }
}

fun <Data: MorphoDataItem.FeedItem> getLikesDataSource(
    descriptor: FeedDescriptor.Likes,
    agent: ButterflyAgent
): MorphoFeedSource<Data> {
    val request: FeedRequest<Data> = { cursor, limit ->
        agent.getActorLikes(descriptor.did, limit, cursor.value).map { response ->
            val newCursor = response.cursor
            val items = response.items
                .map { MorphoDataItem.FeedItem.fromFeedViewPost(it) } as List<Data>
            PagedResponse.Feed(newCursor, items)
        }
    }
    val tuners = agent.id?.let {
        FeedTuner.useFeedTuners(it, agent.prefs, descriptor)
    } ?: listOf<FeedTuner<Data>>()
    return MorphoFeedSource(request, tuners, repliesBumpThreads = true)
}

fun <Data: MorphoDataItem.FeedItem> getAuthorFeedDataSource(
    descriptor: FeedDescriptor.Author,
    agent: ButterflyAgent
): MorphoFeedSource<Data> {
    val request: FeedRequest<Data> = when(descriptor.filter) {
        AuthorFilter.PostsWithReplies -> { cursor, limit ->
            agent.getAuthorFeed(descriptor.did, limit, cursor.value, GetAuthorFeedFilter.POSTS_WITH_REPLIES)
                .map { response ->
                    val newCursor = response.cursor
                    val items = response.items
                        .map { MorphoDataItem.FeedItem.fromFeedViewPost(it) } as List<Data>
                    PagedResponse.Feed(newCursor, items)
                }
        }
        AuthorFilter.PostsNoReplies -> { cursor, limit ->
            agent.getAuthorFeed(descriptor.did, limit, cursor.value, GetAuthorFeedFilter.POSTS_NO_REPLIES)
                .map { response ->
                    val newCursor = response.cursor
                    val items = response.items
                        .map { MorphoDataItem.FeedItem.fromFeedViewPost(it) } as List<Data>
                    PagedResponse.Feed(newCursor, items)
                }
        }
        AuthorFilter.PostsAuthorThreads -> { cursor, limit ->
            agent.getAuthorFeed(descriptor.did, limit, cursor.value, GetAuthorFeedFilter.POSTS_WITH_REPLIES)
                .map { response ->
                    val newCursor = response.cursor
                    val items = response.items
                        .map { MorphoDataItem.FeedItem.fromFeedViewPost(it) } as List<Data>
                    PagedResponse.Feed(newCursor, items)
                }
        }
        AuthorFilter.PostsWithMedia -> { cursor, limit ->
            agent.getAuthorFeed(descriptor.did, limit, cursor.value, GetAuthorFeedFilter.POSTS_WITH_MEDIA)
                .map { response ->
                    val newCursor = response.cursor
                    val items = response.items
                        .map { MorphoDataItem.FeedItem.fromFeedViewPost(it) } as List<Data>
                    PagedResponse.Feed(newCursor, items)
                }
        }
    }
    val tuners = agent.id?.let {
        FeedTuner.useFeedTuners(it, agent.prefs, descriptor)
    } ?: listOf<FeedTuner<Data>>()
    return MorphoFeedSource(request, tuners, repliesBumpThreads = true)
}


fun <Data: MorphoDataItem.FeedItem> getFeedDataSource(
    descriptor: FeedDescriptor.FeedGen,
    agent: ButterflyAgent
): MorphoFeedSource<Data> {
    val request: FeedRequest<Data> = { cursor, limit ->
        agent.api.getFeed(GetFeedQuery(descriptor.uri, limit, cursor.value)).map { response ->
            val newCursor = Cursor(response.cursor)
            val items = response.feed
                .map { MorphoDataItem.FeedItem.fromFeedViewPost(it) } as List<Data>
            PagedResponse.Feed(newCursor, items)
        }
    }
    val tuners = agent.id?.let {
        FeedTuner.useFeedTuners(it, agent.prefs, descriptor)
    } ?: listOf<FeedTuner<Data>>()
    return MorphoFeedSource(request, tuners, repliesBumpThreads = true)
}

fun <Data: MorphoDataItem.FeedItem> getListDataSource(
    descriptor: FeedDescriptor.List,
    agent: ButterflyAgent
): MorphoFeedSource<Data> {
    val request: FeedRequest<Data> = { cursor, limit ->
        agent.api.getListFeed(GetListFeedQuery(descriptor.uri, limit, cursor.value)).map { response ->
            val newCursor = Cursor(response.cursor)
            val items = response.feed
                .map { MorphoDataItem.FeedItem.fromFeedViewPost(it) } as List<Data>
            PagedResponse.Feed(newCursor, items)
        }
    }
    val tuners = agent.id?.let {
        FeedTuner.useFeedTuners(it, agent.prefs, descriptor)
    } ?: listOf<FeedTuner<Data>>()
    return MorphoFeedSource(request, tuners, repliesBumpThreads = true)
}

fun <Data: MorphoDataItem.FeedItem> getTimelineDataSource(
    agent: ButterflyAgent
): MorphoFeedSource<Data> {
    val request: FeedRequest<Data> = { cursor, limit ->
        agent.getTimeline(cursor = cursor.value, limit = limit).map { response ->
            val newCursor = response.cursor
            val items = response.items
                .map { MorphoDataItem.FeedItem.fromFeedViewPost(it) } as List<Data>
            PagedResponse.Feed(newCursor, items)
        }
    }
    val tuners = agent.id?.let {
        FeedTuner.useFeedTuners(it, agent.prefs, FeedDescriptor.Home)
    } ?: listOf<FeedTuner<Data>>()
    return MorphoFeedSource(request, tuners, repliesBumpThreads = true)
}