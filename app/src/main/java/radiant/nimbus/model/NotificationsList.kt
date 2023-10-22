package radiant.nimbus.model

import app.bsky.notification.ListNotificationsNotification
import app.bsky.notification.ListNotificationsReason
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import radiant.nimbus.api.AtUri

/**
 * Notifications combined for display
 * Will show unread if any of a category are unread
 * Categorizes either by what it refers to (for likes and so on) or by type (for follows)
 */
@Serializable
data class NotificationsList(
    private val notifications: List<BskyNotification> = persistentListOf(),
) {
    private var _notificationsList = mutableListOf<NotificationsListItem>()
    var notificationsList: List<NotificationsListItem> = _notificationsList.toList()
        private set
        get() = _notificationsList.toList()

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
    fun concat(new: List<ListNotificationsNotification>): NotificationsList {
        return NotificationsList(
            notifications + new.map {
                it.toBskyNotification()
            }
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