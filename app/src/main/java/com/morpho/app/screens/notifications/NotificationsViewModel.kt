package com.morpho.app.screens.notifications

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import app.bsky.feed.GetPostsQuery
import app.bsky.notification.GetUnreadCountQuery
import app.bsky.notification.ListNotificationsQuery
import app.bsky.notification.UpdateSeenRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import com.morpho.app.MorphoApplication
import com.morpho.butterfly.AtUri
import com.morpho.app.butterfly
import com.morpho.app.base.BaseViewModel
import com.morpho.app.model.NotificationsList
import com.morpho.app.model.toPost
import javax.inject.Inject

data class NotificationsState(
    val isLoading : Boolean = false,
    val cursor: String? = null,
    val hideRead: Boolean = false,
    val showPosts: Boolean = true,
)

data class NotificationsFilter(
    val likes: Boolean = true,
    val reposts: Boolean = true,
    val follows: Boolean = true,
    val mentions: Boolean = true,
    val quotes: Boolean = true,
    val replies: Boolean = true,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(NotificationsState())
        private set

    private val _notifications = MutableStateFlow(NotificationsList())
    val notifications = _notifications.asStateFlow()
    var notificationsFilter by mutableStateOf(NotificationsFilter())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    lateinit var unreadCount: StateFlow<Long>

    fun connectNotifications(unread: StateFlow<Long>) {
        unreadCount = unread
    }

    suspend fun getUnread() = viewModelScope.async(Dispatchers.IO) {
         getApplication<MorphoApplication>().butterfly.api.getUnreadCount(GetUnreadCountQuery())
             .onFailure {
                Log.e("Notifications", it.toString())
                return@async (-1)
             }
             .onSuccess {
                 return@async it.count
             }
    }

    fun getNotifications(cursor: String? = null) = viewModelScope.launch {
        launch(Dispatchers.IO) {
            getApplication<MorphoApplication>().butterfly.api.listNotifications(
                ListNotificationsQuery(
                    limit = 50,
                    cursor = cursor,
                )
            ).onSuccess { response ->
                if (cursor == null) {
                    _notifications.update {
                        NotificationsList(it.notifications)
                    }
                } else {
                    _notifications.update { old ->
                        old.concat(response.notifications)
                    }
                }
                state = state.copy(isLoading = false,
                    cursor = response.cursor)
            }
        }
        _isRefreshing.emit(false)
    }

    fun updateSeen() = viewModelScope.launch(Dispatchers.IO) {
        getApplication<MorphoApplication>().butterfly.api.updateSeen(UpdateSeenRequest(Clock.System.now()))
    }

    fun toggleUnread() {
        state = state.copy(hideRead = !state.hideRead)
    }

    suspend fun getPost(uri: AtUri) = viewModelScope.async(Dispatchers.IO) {
        return@async getApplication<MorphoApplication>().butterfly.api.getPosts(GetPostsQuery(persistentListOf(uri))).getOrNull()?.posts?.first()?.toPost()
    }
}


