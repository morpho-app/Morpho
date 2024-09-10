package com.morpho.app.screens.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.morpho.app.model.uidata.AtCursor
import com.morpho.app.model.uidata.initAtCursor
import com.morpho.app.model.uistate.NotificationsUIState
import com.morpho.app.model.uistate.UiLoadingState
import com.morpho.app.screens.base.BaseScreenModel
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TabbedNotificationScreenModel: BaseScreenModel() {

    private val cursorFlow = initAtCursor()

    private var showPosts by mutableStateOf(true)
    private var _uiState: MutableStateFlow<NotificationsUIState> =
        MutableStateFlow(
            NotificationsUIState(
                notifService.notifications,
                notifService.filter,
                showPosts,
                UiLoadingState.Loading
            )
        )

    init {
        viewModelScope.launch {
            val f = notifService.notifications(cursorFlow).map { it.getOrNull() }
            cursorFlow.emit(AtCursor.EMPTY)
            f.collect {
                if(it != null) {
                    _uiState.update {
                        NotificationsUIState(
                            notifService.notifications,
                            notifService.filter,
                            showPosts,
                            UiLoadingState.Idle
                        )
                    }
                }
            }
        }
    }

    val uiState: StateFlow<NotificationsUIState>
        get() = _uiState.asStateFlow()

    fun markAllRead() {
        notifService.updateNotificationsSeen()
    }

    fun markAsRead(uri: AtUri) {
        notifService.markAsRead(uri)
    }

    fun refreshNotifications(cursor: AtCursor): Boolean {
        return cursorFlow.tryEmit(cursor)
    }
}