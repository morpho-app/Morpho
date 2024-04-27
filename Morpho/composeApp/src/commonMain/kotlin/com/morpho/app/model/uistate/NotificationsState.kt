package com.morpho.app.model.uistate

import com.morpho.app.model.bluesky.NotificationsList
import com.morpho.app.model.bluesky.NotificationsListItem
import com.morpho.app.model.uidata.AtCursor
import com.morpho.app.model.uidata.filterNotifications
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent




@Serializable
data class NotificationsUIState(
    private val notificationsList: StateFlow<NotificationsList> = MutableStateFlow(NotificationsList()),
    val filterState: StateFlow<NotificationsFilterState> = MutableStateFlow(NotificationsFilterState()),
    val showPosts: Boolean = true,
    override val loadingState: UiLoadingState = UiLoadingState.Loading,
): KoinComponent, UiState {

    val cursor:AtCursor
        get() = notificationsList.value.cursor

    val notifications: Flow<ImmutableList<NotificationsListItem>>
        get() = notificationsList.map {
            filterNotifications(it.notificationsList, filterState.value)
        }

    //@NativeCoroutines
    val numberUnread: Flow<Int>
        get() = notifications.map { items -> items.filterNot { it.isRead }.size }

}

@Serializable
data class NotificationsFilterState(
    val showAlreadyRead: Boolean = true,
    val showLikes:  Boolean = true,
    val showReposts: Boolean = true,
    val showFollows: Boolean = true,
    val showMentions: Boolean = true,
    val showQuotes: Boolean = true,
    val showReplies: Boolean = true,
)