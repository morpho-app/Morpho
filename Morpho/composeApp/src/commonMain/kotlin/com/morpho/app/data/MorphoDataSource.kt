package com.morpho.app.data

import androidx.compose.ui.util.fastAny
import app.cash.paging.PagingConfig
import app.cash.paging.PagingSource
import app.cash.paging.PagingState
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uidata.ContentLabelService
import com.morpho.app.model.uidata.Delta
import com.morpho.app.model.uidata.Moment
import com.morpho.butterfly.ButterflyAgent
import com.morpho.butterfly.Cursor
import com.morpho.butterfly.FeedRequest
import com.morpho.butterfly.PagedResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration


abstract class MorphoDataSource<Data:Any>: PagingSource<Cursor, Data>(), KoinComponent {
    val agent: MorphoAgent by inject()
    val moderator: ContentLabelService by inject()

    override fun getRefreshKey(state: PagingState<Cursor, Data>): Cursor? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            if (anchorPage?.prevKey == null) return Cursor.Empty // First page
            else if (anchorPage.nextKey == null) anchorPage.prevKey // Last page
            else anchorPage.prevKey // Initial page
        }
    }
    companion object {
        val defaultConfig = PagingConfig(
            pageSize = 20,
            prefetchDistance = 10,
            initialLoadSize = 50,
            enablePlaceholders = true,
        )
    }
}


data class MorphoFeedSource<Data : MorphoDataItem.FeedItem>(
    val request: FeedRequest<Data>,
    val tuners: List<FeedTuner<Data>> = listOf(),
    val repliesBumpThreads: Boolean = false,
    val collectThreads: Boolean = true,
): MorphoDataSource<Data>() {
    override suspend fun load(
        params: LoadParams<Cursor>
    ): LoadResult<Cursor,Data> {
        try {
            val limit = params.loadSize
            val loadCursor = when(params) {
                is LoadParams.Append -> params.key
                is LoadParams.Prepend -> Cursor.Empty
                is LoadParams.Refresh -> Cursor.Empty
            }
            return request(loadCursor, limit.toLong()).map {
                pagedList ->

                val tunedList =  when(pagedList) {
                    is PagedResponse.Feed -> {
                        var tunedFeed = pagedList.copy(
                            items =  if(collectThreads) {
                                pagedList.items.filter { !moderator.shouldHideItem(it) }
                                    .collectThreads(
                                        repliesBumpThreads = repliesBumpThreads,
                                        agent = agent
                                    ).getOrNull() ?: pagedList.items
                            } else pagedList.items
                        )
                        tuners.forEach { tuner ->
                            tunedFeed = tuner.tune(tunedFeed)
                        }
                        tunedFeed
                    }
                    is PagedResponse.FromRecord -> pagedList.items
                    is PagedResponse.Profile -> pagedList.items
                }
                LoadResult.Page(
                    data = tunedList as List<Data>,
                    prevKey = when(params) {
                        is LoadParams.Append -> loadCursor
                        is LoadParams.Prepend -> Cursor.Empty
                        is LoadParams.Refresh -> Cursor.Empty
                    },
                    nextKey = pagedList.cursor,
                )
            }.onFailure {
                return LoadResult.Error(it)
            }.getOrDefault(LoadResult.Error(Exception("Load failed")))
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    fun updates(): Flow<Data> = flow {
        while (true) {
            val newData = peek().getOrNull()
            if(newData != null) {
                emit(newData)
            }
        }
    }.distinctUntilChanged().flowOn(Dispatchers.Default)

    suspend fun hasNew(): Boolean {
        return peek().getOrNull() != null
    }

    suspend fun peek(): Result<Data?> {
        try {
            request(Cursor.Empty, 1).onSuccess { pagedList ->
                return Result.success(pagedList.items.firstOrNull())
            }.onFailure {
                return Result.failure(it)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.failure(Exception("Should not be reached"))
    }
}

suspend fun <Data: MorphoDataItem> List<Data>.collectThreads(
    depth: Int = 3, height: Int = 80,
    timeRange: Delta = Delta(Duration.parse("4h")),
    repliesBumpThreads: Boolean = true,
    agent: ButterflyAgent? = null, // allows to just use local data
): Result<List<Data>> {
    val threads = mutableListOf<MorphoDataItem.Thread?>()
    val replies = mutableListOf<MorphoDataItem.Post?>()
    val posts = mutableListOf<MorphoDataItem.Post?>()
    val threadCandidates = mutableListOf<MorphoDataItem.Thread?>()
    this.forEach { item ->
        when(item) {
            is MorphoDataItem.Post -> {
                if (item.isReply) replies.add(item)
                else if (item.isOrphan) posts.add(item)
                else posts.add(item)
            }
            is MorphoDataItem.Thread -> {
                if (!item.isIncompleteThread) threads.add(item)
                else threadCandidates.add(item)
            }
            else -> return Result.failure(Exception("Invalid feed item type"))
        }
    }
    replies.forEachIndexed { index, reply ->
        if (reply == null) return@forEachIndexed
        if (reply.isOrphan) {
            val parent = reply.post.reply?.parentPost
                ?: reply.post.reply?.replyRef?.parent?.uri?.let {
                    agent?.getPosts(listOf(it))?.getOrNull()?.firstOrNull()?.toPost()
                }
            val root = reply.post.reply?.rootPost
                ?: reply.post.reply?.replyRef?.root?.uri?.let {
                    agent?.getPosts(listOf(it))?.getOrNull()?.firstOrNull()?.toPost()
                }
            replies[index] = MorphoDataItem.Post(
                reply.post.copy(reply = reply.post.reply?.copy(parentPost = parent, rootPost = root)),
                reply.reason,
                isOrphan = root != null && parent != null,
            )
        }
        val newReply = replies[index] ?: return@forEachIndexed // Update in case we changed it above
        val replyRef = newReply.post.reply?.replyRef ?: return@forEachIndexed
        val parent = replyRef.parent.uri
        val root = replyRef.root.uri
        val inThread = threads.indexOfFirst { it?.containsUri(parent) ?: false  || it?.containsUri(root) ?: false }
        if (inThread != -1) {
            val thread = threads.getOrNull(inThread) ?: return@forEachIndexed
            threads[inThread] = thread.addReply(newReply.post)
            replies[index] = null
        }
        val inCandidates = threadCandidates.indexOfFirst {  it?.containsUri(parent) ?: false  || it?.containsUri(root) ?: false }
        if (inCandidates != -1) {
            val thread = threadCandidates.getOrNull(inCandidates) ?: return@forEachIndexed
            threadCandidates[inCandidates] = thread.addReply(newReply.post)
            replies[index] = null
        }

    }
    threadCandidates.forEachIndexed { index, thread ->
        if (thread == null) return@forEachIndexed
        val rootInThreads = threads.indexOfFirst { t -> t?.containsUri(thread.rootUri) ?: false }
        if (rootInThreads == - 1) {
            val threadToSplice = threads.getOrNull(rootInThreads) ?: return@forEachIndexed
            if(
                thread.thread.parents.firstOrNull() is ThreadPost.ViewablePost
                && threadToSplice.thread.parents.firstOrNull() is ThreadPost.ViewablePost
                && thread.rootUri == threadToSplice.rootUri
            ) {
                if(thread.thread.parents.size == 1 && threadToSplice.thread.parents.size == 1) {
                    // Both threads have the same, viewable root post and are only one level deep in terms of parents
                    val newEntry = thread.thread.parents.first() as ThreadPost.ViewablePost
                    val oldEntry = threadToSplice.thread.parents.first() as ThreadPost.ViewablePost

                    val newReplies = (newEntry.replies + oldEntry.replies).distinctBy { it.uri }.toMutableList()
                    newReplies.add(ThreadPost.ViewablePost(thread.thread.post, thread.thread.parents.last(), thread.thread.replies))
                    if( thread.getUri() != threadToSplice.getUri() )
                        newReplies.add(ThreadPost.ViewablePost(threadToSplice.thread.post, threadToSplice.thread.parents.last(),threadToSplice.thread.replies))
                    val newThread = BskyPostThread(
                        post = newEntry.post,
                        parents = listOf(),
                        replies = newReplies.distinctBy { it.uri },
                    )
                    threads[rootInThreads] = threadToSplice.copy(thread = newThread, isIncompleteThread = false)
                    threadCandidates[index] = null
                } else if(thread.thread.parents.size == 2 && threadToSplice.thread.parents.size == 2) {
                    // Both threads have the same, viewable root post and parent chains are both length 2
                    val newEntry = thread.thread.parents.first() as ThreadPost.ViewablePost

                    val newReplies = mutableListOf<ThreadPost>()
                    if(thread.thread.parents.lastOrNull() !is ThreadPost.ViewablePost) return@forEachIndexed
                    if(threadToSplice.thread.parents.lastOrNull() !is ThreadPost.ViewablePost) return@forEachIndexed
                    val newParent = thread.thread.parents.last() as ThreadPost.ViewablePost
                    val oldParent = threadToSplice.thread.parents.last() as ThreadPost.ViewablePost
                    val newReply = ThreadPost.ViewablePost(thread.thread.post, thread.thread.parents.last(), thread.thread.replies)
                    val oldReply = ThreadPost.ViewablePost(threadToSplice.thread.post, threadToSplice.thread.parents.last(), threadToSplice.thread.replies)
                    newParent.addReply(newReply)
                    oldParent.addReply(oldReply)
                    newReplies.add(newReply)
                    newReplies.add(oldReply)
                    val newThread = BskyPostThread(
                        post = newEntry.post,
                        parents = listOf(newParent),
                        replies = newReplies.distinctBy { it.uri },
                    )
                    threads[rootInThreads] = threadToSplice.copy(thread = newThread, isIncompleteThread = false)
                    threadCandidates[index] = null
                }

            }
        } else {
            val inThreads = threads.indexOfFirst { t -> t?.containsUri(thread.thread.post.uri) ?: false }
            if (inThreads == - 1) {
                val threadToSplice = threads.getOrNull(index) ?: return@forEachIndexed
                threads[index] = threadToSplice.addReply(ThreadPost.ViewablePost(thread.thread.post, thread.thread.parents.last(), thread.thread.replies))
                threadCandidates[index] = null
            }
        }
    }
    threadCandidates.filterNotNull()
    if (threadCandidates.isNotEmpty()) threads.addAll(threadCandidates)
    val newReplies = replies.filterNotNull()
        .distinctBy { it.getUri() }
        .filterNot { reply ->
            if(reply.isRepost) return@filterNot false
            if(reply.isQuotePost) return@filterNot false
            reply.getUris().any { uri -> threads.any { it?.containsUri(uri) ?: false } }
        }.sortedByDescending { when(it.reason) {
            is BskyPostReason.BskyPostRepost -> it.reason.indexedAt
            else -> it.post.createdAt
        } }.iterator()
    var newPosts = posts.toList().filterNotNull()
    newPosts = newPosts.distinctBy { it.getUri() }
    newPosts = newPosts.filterNot { post ->
        if(post.isRepost) return@filterNot false
        if(post.isQuotePost) return@filterNot false
        post.getUris().any { uri -> threads.any { it?.containsUri(uri) ?: false } }
    }.sortedByDescending { when(it.reason) {
        is BskyPostReason.BskyPostRepost -> it.reason.indexedAt
        else -> it.post.createdAt
    } }
    val newPostsIter = newPosts.iterator()
    var newThreads = threads.toList().filterNotNull()
    newThreads = newThreads.sortedByDescending { if(!repliesBumpThreads) {
        it.rootAccessiblePost.createdAt
    } else {
        maxOf(it.thread.post.createdAt,
              it.thread.replies.fold(it.thread.post.createdAt) { acc, post ->
                  val postTime = when(post) {
                      is ThreadPost.ViewablePost -> post.post.createdAt
                      is ThreadPost.BlockedPost -> Moment(Instant.DISTANT_PAST)
                      is ThreadPost.NotFoundPost -> Moment(Instant.DISTANT_PAST)
                  }
                  maxOf(acc, postTime)
              })
    } }
    newThreads = newThreads.distinctBy { it.getUri() }
        .filterNot { thread ->
            thread.getUris().filterNot { uri ->
                newThreads.fastAny { it.getUri() == uri } }.size > 1
        }
    val newThreadsIter = newThreads.iterator()
    val newFeed = mutableListOf<MorphoDataItem.FeedItem>()
    while(newPostsIter.hasNext() || newThreadsIter.hasNext() || newReplies.hasNext() ) {
        if(newPostsIter.hasNext()) newFeed.add(newPostsIter.next())
        if(newThreadsIter.hasNext()) newFeed.add(newThreadsIter.next())
        if(newReplies.hasNext()) newFeed.add(newReplies.next())
    }
    val dedupedFeed = newFeed.distinctBy { it.getUri() }
    val sortedFeed = dedupedFeed.sortedByDescending {
        when(it) {
            is MorphoDataItem.Post -> when(it.reason) {
                is BskyPostReason.BskyPostFeedPost -> it.post.createdAt
                is BskyPostReason.BskyPostRepost -> it.reason.indexedAt
                is BskyPostReason.SourceFeed -> it.post.createdAt
                null -> it.post.createdAt
            }
            is MorphoDataItem.Thread -> if(!repliesBumpThreads) {
                it.rootAccessiblePost.createdAt
            } else {
                maxOf(it.thread.post.createdAt,
                      it.thread.replies.fold(it.thread.post.createdAt) { acc, post ->
                          val postTime = when(post) {
                              is ThreadPost.ViewablePost -> post.post.createdAt
                              is ThreadPost.BlockedPost -> Moment(Instant.DISTANT_PAST)
                              is ThreadPost.NotFoundPost -> Moment(Instant.DISTANT_PAST)
                          }
                          maxOf(acc, postTime)
                      })
            }

        }
    }
    return Result.success(sortedFeed as List<Data>)
}

