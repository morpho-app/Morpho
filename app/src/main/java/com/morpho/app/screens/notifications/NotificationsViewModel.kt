package morpho.app.screens.notifications

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import app.bsky.feed.GetPostsQueryParams
import app.bsky.notification.GetUnreadCountQueryParams
import app.bsky.notification.ListNotificationsQueryParams
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
import morpho.app.api.AtUri
import morpho.app.api.response.AtpResponse
import com.morpho.app.apiProvider
import morpho.app.base.BaseViewModel
import morpho.app.model.NotificationsList
import morpho.app.model.toBskyNotification
import morpho.app.model.toPost
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
        return@async when(
            val response = getApplication<MorphoApplication>().apiProvider.api.getUnreadCount(GetUnreadCountQueryParams())
        ) {
            is AtpResponse.Failure -> {
                -1
            }

            is AtpResponse.Success -> {
                response.response.count
            }
        }
    }

    fun getNotifications(cursor: String? = null) = viewModelScope.launch {
        launch(Dispatchers.IO) {
            when(
                val response = getApplication<MorphoApplication>().apiProvider.api.listNotifications(
                    ListNotificationsQueryParams(
                    limit = 50,
                    cursor = cursor,
                )
                )
            ) {
                is AtpResponse.Failure -> {}
                is AtpResponse.Success -> {
                    if (cursor == null) {
                        _notifications.update {
                            NotificationsList(response.response.notifications.map {
                                it.toBskyNotification()
                            })
                        }
                    } else {
                        _notifications.update { old ->
                            old.concat(response.response.notifications)
                        }
                    }
                    state = state.copy(isLoading = false,
                        cursor = response.response.cursor)
                }
            }
        }
        _isRefreshing.emit(false)
    }

    fun updateSeen() = viewModelScope.launch(Dispatchers.IO) {
        getApplication<MorphoApplication>().apiProvider.api.updateSeen(UpdateSeenRequest(Clock.System.now()))
    }

    fun toggleUnread() {
        state = state.copy(hideRead = !state.hideRead)
    }

    suspend fun getPost(uri: AtUri) = viewModelScope.async(Dispatchers.IO) {
        when(val response = getApplication<MorphoApplication>().apiProvider.api.getPosts(GetPostsQueryParams(
            persistentListOf(uri)
        ))) {
            is AtpResponse.Failure -> return@async null
            is AtpResponse.Success -> {
                return@async response.response.posts.first().toPost()
            }
        }
    }
}

