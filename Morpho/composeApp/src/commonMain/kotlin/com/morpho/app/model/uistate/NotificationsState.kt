package com.morpho.app.model.uistate

import com.morpho.app.model.bluesky.BskyNotification
import com.morpho.app.model.bluesky.NotificationsList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


@Serializable
data class NotificationsBackendState(
    val notifications: StateFlow<ImmutableList<BskyNotification>> = MutableStateFlow(persistentListOf()),
    val loadingState: ContentLoadingState = ContentLoadingState.Idle,
    val hasNewNotifications: Boolean = false,
) {
    val isLoading: Boolean
        get() = loadingState == ContentLoadingState.Loading

    //@NativeCoroutines
    val numberUnread: Int = notifications.value.size
}


@Serializable
data class NotificationsUIState(
    override val loadingState: UiLoadingState = UiLoadingState.Loading,
): KoinComponent, UiState {

    val backendState: NotificationsBackendState by inject()

    val notifications: Flow<NotificationsList>
        get() = backendState.notifications.map { NotificationsList(it) }.distinctUntilChanged()
    val isLoadingNotifications: Boolean
        get() = backendState.isLoading

    //@NativeCoroutines
    val numberUnread: Int = backendState.numberUnread
}