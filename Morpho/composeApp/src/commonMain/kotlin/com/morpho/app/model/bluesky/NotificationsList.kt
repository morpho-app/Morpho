package com.morpho.app.model.bluesky

import androidx.compose.ui.util.fastMap
import app.bsky.notification.ListNotificationsNotification
import app.bsky.notification.ListNotificationsReason
import com.morpho.app.model.uidata.AtCursor
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.Serializable

/**
 * Notifications combined for display
 * Will show unread if any of a category are unread
 * Categorizes either by what it refers to (for likes and so on) or by type (for follows)
 */
@Serializable
data class NotificationsList(
    private val notifications: List<BskyNotification> = persistentListOf(),
    val cursor: AtCursor = AtCursor.EMPTY,
) {
    private var _notificationsList: MutableList<MutableNotificationsListItem> = mutableListOf()
    val notificationsList: List<NotificationsListItem>
        get() {
            if (!initialized) {
                initList()
            }
            return _notificationsList.fastMap { it.toImmutable() }.toList()
        }

    private var initialized = false
    init {
        initList()
    }

    private fun initList() {
        if (initialized) return
        val seen = mutableListOf<AtUri>()
        notifications.map { notif ->
            if(notif.reasonSubject != null && seen.contains(notif.reasonSubject)) {
                val index = _notificationsList.indexOfFirst {
                    it.reasonSubject == notif.reasonSubject
                }
                if (index >= 0 && notif.reason == _notificationsList[index].reason) {
                    _notificationsList[index].notifications.add(notif)
                    _notificationsList[index].isRead = if (notif.isRead) true else _notificationsList[index].isRead
                } else {
                    _notificationsList.add(
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
                _notificationsList.add(
                    MutableNotificationsListItem(
                        notifications = mutableListOf(notif),
                        reason = notif.reason,
                        isRead = notif.isRead,
                        reasonSubject = notif.reasonSubject
                    )
                )
            } else {
                val index = _notificationsList.indexOfFirst { item->
                    item.reason == notif.reason
                }
                if (index >= 0) {
                    _notificationsList[index].notifications.add(notif)
                    _notificationsList[index].isRead = if (notif.isRead) true else _notificationsList[index].isRead
                } else {
                    _notificationsList.add(
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
        initialized = true
    }
    fun concat(new: List<ListNotificationsNotification>): NotificationsList {
        return NotificationsList(
            notifications.toPersistentList().addAll(new.map {
                it.toBskyNotification()
            })
        )
    }

    fun concat(new: NotificationsList): NotificationsList {
        return NotificationsList(
            notifications.toPersistentList().addAll(new.notifications)
        )
    }
    fun markAllRead(): NotificationsList {
        _notificationsList.map {
            it.isRead = true
        }
        val newNotifs = notifications.mapImmutable {
            when(it) {
                is BskyNotification.Follow -> it.copy(isRead = true)
                is BskyNotification.Like -> it.copy(isRead = true)
                is BskyNotification.Post -> it.copy(isRead = true)
                is BskyNotification.Repost -> it.copy(isRead = true)
                is BskyNotification.Unknown -> it.copy(isRead = true)
            }
        }
        return this.copy(
            notifications = newNotifs
        )
    }

    fun markRead(uri: AtUri): NotificationsList {
        _notificationsList.forEach { notificationsListItem ->
            if(notificationsListItem.notifications.firstOrNull { it.uri == uri } != null) {
                notificationsListItem.isRead = true
                notificationsListItem.notifications.map {
                    when(it) {
                        is BskyNotification.Follow -> it.copy(isRead = true)
                        is BskyNotification.Like -> it.copy(isRead = true)
                        is BskyNotification.Post -> it.copy(isRead = true)
                        is BskyNotification.Repost -> it.copy(isRead = true)
                        is BskyNotification.Unknown -> it.copy(isRead = true)
                    }
                }
            }
        }
        return this
    }
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
                notifications = item.notifications.toMutableList(),
                reason = item.reason,
                isRead = item.isRead,
                reasonSubject = item.reasonSubject
            )
        }
    }
    fun toImmutable(): NotificationsListItem {
        return NotificationsListItem(
            notifications = notifications.toImmutableList(),
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
                notifications = item.notifications.toImmutableList(),
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