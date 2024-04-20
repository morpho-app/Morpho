package com.morpho.app.model.bluesky

import app.bsky.notification.ListNotificationsNotification
import app.bsky.notification.ListNotificationsReason
import com.morpho.butterfly.AtUri
import kotlinx.collections.immutable.ImmutableList
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
    val notifications: ImmutableList<BskyNotification> = persistentListOf(),
) {
    private lateinit var _notificationsList: MutableList<NotificationsListItem>
    val notificationsList: ImmutableList<NotificationsListItem>
        get() = _notificationsList.toImmutableList()

    init {
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
                        NotificationsListItem(
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
                    NotificationsListItem(
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
                        NotificationsListItem(
                            notifications = mutableListOf(notif),
                            reason = notif.reason,
                            isRead = notif.isRead,
                            reasonSubject = notif.reasonSubject
                        )
                    )
                }
            }
        }
    }
    fun concat(new: ImmutableList<ListNotificationsNotification>): NotificationsList {
        return NotificationsList(
            notifications.toPersistentList().addAll(new.map {
                it.toBskyNotification()
            })
        )
    }
}

@Serializable
data class NotificationsListItem(
    val notifications: MutableList<BskyNotification> = mutableListOf(),
    val reason: ListNotificationsReason,
    var isRead: Boolean = false,
    val reasonSubject: AtUri? = null,
)