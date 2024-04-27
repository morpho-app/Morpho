package com.morpho.app.screens.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.uidata.AtCursor
import com.morpho.app.model.uidata.initAtCursor
import com.morpho.app.model.uistate.NotificationsUIState
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
                showPosts
            )
        )

    init {
        screenModelScope.launch {
            val f = notifService.notifications(cursorFlow).map { it.getOrNull() }
            cursorFlow.emit(null)
            if(f.first()!= null) {
                _uiState.update { NotificationsUIState(
                    notifService.notifications,
                    notifService.filter,
                    showPosts
                ) }
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