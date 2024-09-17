package com.morpho.app.model.uistate

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent


@Serializable
data class NotificationsUIState(
    val filterState: MutableStateFlow<NotificationsFilterState> = MutableStateFlow(NotificationsFilterState()),
    val showPosts: Boolean = true,
): KoinComponent
@Immutable
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