package com.morpho.app.data

import app.bsky.notification.ListNotificationsReason
import app.cash.paging.PagingConfig
import app.cash.paging.compose.LazyPagingItems
import com.morpho.app.model.bluesky.BskyNotification
import com.morpho.app.model.bluesky.toBskyNotification
import com.morpho.app.model.uistate.NotificationsFilterState
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cursor
import kotlinx.serialization.Serializable
import org.lighthousegames.logging.logging

class NotificationsSource: MorphoDataSource<BskyNotification>() {
    companion object {
        val log = logging()
        val defaultConfig = PagingConfig(
            pageSize = 20,
            prefetchDistance = 20,
            initialLoadSize = 50,
            enablePlaceholders = false,
        )
    }

    override suspend fun load(params: LoadParams<Cursor>): LoadResult<Cursor, BskyNotification> {
        try {
            val limit = params.loadSize
            val loadCursor = when(params) {
                is LoadParams.Append -> params.key
                is LoadParams.Prepend -> Cursor.Empty
                is LoadParams.Refresh -> Cursor.Empty
            }
            return agent.listNotifications(limit.toLong(), loadCursor.value).map { response ->
                val newCursor = response.cursor
                val items = response.items.map { it.toBskyNotification()}
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

fun LazyPagingItems<BskyNotification>.collectNotifications(
    toMark: List<AtUri> = listOf()
) : List<NotificationsListItem> {
    val seen = mutableListOf<AtUri>()
    val workList = mutableListOf<MutableNotificationsListItem>()
    this.itemSnapshotList.map { notif ->
        if (notif == null) return@map NotificationsListItem(
            notifications = listOf(),
            reason = ListNotificationsReason.PLACEHOLDER,
            isRead = false,
            reasonSubject = null,
        )
        if(notif.reasonSubject != null && seen.contains(notif.reasonSubject)) {
            val index = workList.indexOfFirst {
                it.reasonSubject == notif.reasonSubject
            }
            if (index >= 0 && notif.reason == workList[index].reason) {
                workList[index].notifications.add(notif)
                workList[index].isRead = if (notif.isRead) true else workList[index].isRead
            } else {
                workList.add(
                    MutableNotificationsListItem(
                        notifications = mutableListOf(notif),
                        reason = notif.reason,
                        isRead = notif.isRead,
                        reasonSubject = notif.reasonSubject
                    )
                )
            }
        } else if (notif.reasonSubject != null) {
            seen.add(notif.reasonSubject!!)
            workList.add(
                MutableNotificationsListItem(
                    notifications = mutableListOf(notif),
                    reason = notif.reason,
                    isRead = notif.isRead,
                    reasonSubject = notif.reasonSubject
                )
            )
        } else {
            val index = workList.indexOfFirst { item->
                item.reason == notif.reason
            }
            if (index >= 0) {
                workList[index].notifications.add(notif)
                workList[index].isRead = if (notif.isRead) true else workList[index].isRead
            } else {
                workList.add(
                    MutableNotificationsListItem(
                        notifications = mutableListOf(notif),
                        reason = notif.reason,
                        isRead = notif.isRead,
                        reasonSubject = notif.reasonSubject
                    )
                )
            }
        }
    }
    return workList.map { it.toImmutable() }
}

fun List<NotificationsListItem>.filterNotifications(
    filter: NotificationsFilterState,
): List<NotificationsListItem> {
    return this.filter {
        (if(it.isRead) filter.showAlreadyRead else true) &&
                when(it.reason) {
                    ListNotificationsReason.LIKE -> filter.showLikes
                    ListNotificationsReason.REPOST -> filter.showReposts
                    ListNotificationsReason.FOLLOW -> filter.showFollows
                    ListNotificationsReason.MENTION -> filter.showMentions
                    ListNotificationsReason.REPLY -> filter.showReplies
                    ListNotificationsReason.QUOTE -> filter.showQuotes
                    else -> true
                }
    }.toList()
}

@Serializable
data class MutableNotificationsListItem(
    val notifications: MutableList<BskyNotification> = mutableListOf(),
    val reason: ListNotificationsReason,
    var isRead: Boolean = false,
    val reasonSubject: AtUri? = null,
) {
    companion object {
        fun fromImmutable(item: NotificationsListItem): MutableNotificationsListItem {
            return MutableNotificationsListItem(
                notifications = item.notifications.distinctBy { it.author.did }.toMutableList(),
                reason = item.reason,
                isRead = item.isRead,
                reasonSubject = item.reasonSubject
            )
        }
    }
    fun toImmutable(): NotificationsListItem {
        return NotificationsListItem(
            notifications = notifications.distinctBy { it.author.did },
            reason = reason,
            isRead = isRead,
            reasonSubject = reasonSubject
        )
    }
}

@Serializable
data class NotificationsListItem(
    val notifications: List<BskyNotification>,
    val reason: ListNotificationsReason,
    val isRead: Boolean,
    val reasonSubject: AtUri?,
) {
    companion object {
        fun fromMutable(item: MutableNotificationsListItem) {
            NotificationsListItem(
                notifications = item.notifications.distinctBy { it.author.did },
                reason = item.reason,
                isRead = item.isRead,
                reasonSubject = item.reasonSubject
            )
        }
    }

    override fun hashCode(): Int {
        return notifications.hashCode() + reason.hashCode() + reasonSubject.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NotificationsListItem

        if (reason != other.reason) return false
        if (reasonSubject != other.reasonSubject) return false
        if (notifications != other.notifications) return false

        return true
    }
}