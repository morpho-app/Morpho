package com.morpho.app.model.uidata

import app.bsky.feed.GetAuthorFeedFilter
import app.bsky.feed.GetFeedQuery
import app.bsky.feed.GetListFeedQuery
import app.cash.paging.InvalidatingPagingSourceFactory
import app.cash.paging.Pager
import app.cash.paging.cachedIn
import com.morpho.app.data.FeedTuner
import com.morpho.app.data.MorphoDataSource
import com.morpho.app.data.MorphoFeedSource
import com.morpho.app.model.bluesky.AuthorFilter
import com.morpho.app.model.bluesky.FeedDescriptor
import com.morpho.app.model.bluesky.FeedSourceInfo
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.butterfly.ButterflyAgent
import com.morpho.butterfly.Cursor
import com.morpho.butterfly.FeedRequest
import com.morpho.butterfly.PagedResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map


class FeedPresenter<E: FeedEvent>(
    var descriptor: FeedDescriptor? = null,
): PagedPresenter<MorphoDataItem.FeedItem, E>() {

    private var pagerFactory = InvalidatingPagingSourceFactory<Cursor, MorphoDataItem.FeedItem> {
        descriptor?.getDataSource(agent) ?: getTimelineDataSource(agent)
    }

    override var pager: Pager<Cursor, MorphoDataItem.FeedItem> = run {
        val pagingConfig = MorphoDataSource.defaultConfig
        Pager(pagingConfig) {
            pagerFactory.invoke()
        }
    }



    override fun <E: Event> produceUpdates(events: Flow<E>): Flow<UIUpdate> = events.filter {
        it is FeedEvent.Load && it.descriptor == descriptor
    }.map { event ->
        when(event) {
            is FeedEvent.Load -> {
                when(event.descriptor) {
                    is FeedDescriptor.Author -> AuthorFeedUpdate.Feed(
                        event.descriptor.did, event.descriptor.filter, pager.flow.cachedIn(presenterScope))
                    is FeedDescriptor.FeedGen -> {
                        FeedUpdate.Feed(event.uri, pager.flow.cachedIn(presenterScope))
                    }
                    FeedDescriptor.Home -> FeedUpdate.Feed(
                        FeedSourceInfo.Home.uri, pager.flow.cachedIn(presenterScope))
                    is FeedDescriptor.Likes -> AuthorFeedUpdate.Likes(
                        event.descriptor.did, pager.flow.cachedIn(presenterScope))
                    is FeedDescriptor.List -> {
                        FeedUpdate.Feed(event.uri, pager.flow.cachedIn(presenterScope))
                    }
                }
            }
            else -> UIUpdate.NoOp
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

@Suppress("UNCHECKED_CAST")
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