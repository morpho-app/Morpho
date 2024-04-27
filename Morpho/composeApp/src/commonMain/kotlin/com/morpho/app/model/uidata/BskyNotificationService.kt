package com.morpho.app.model.uidata

import app.bsky.notification.GetUnreadCountQuery
import app.bsky.notification.ListNotificationsQuery
import app.bsky.notification.ListNotificationsReason
import app.bsky.notification.UpdateSeenRequest
import com.morpho.app.di.UpdateTick
import com.morpho.app.model.bluesky.NotificationsList
import com.morpho.app.model.bluesky.NotificationsListItem
import com.morpho.app.model.bluesky.toBskyNotification
import com.morpho.app.model.uistate.NotificationsFilterState
import com.morpho.app.util.mapImmutable
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Butterfly
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

class BskyNotificationService: KoinComponent {
    val api: Butterfly by inject()

    private val mutex = Mutex()

    private var _notifications = MutableStateFlow(NotificationsList())

    val notifications: StateFlow<NotificationsList>
        get() = _notifications.asStateFlow()

    private var _filter = MutableStateFlow(NotificationsFilterState())

    val filter: StateFlow<NotificationsFilterState>
        get() = _filter.asStateFlow()


    companion object {
        val log = logging()
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    fun updateFilter(filterState: NotificationsFilterState) = serviceScope.launch {
        mutex.withLock {
            _filter.update { filterState }
        }
    }

    fun updateNotificationsSeen() = serviceScope.launch {
        api.api.updateSeen(UpdateSeenRequest(Clock.System.now()))
        _notifications.update { it.markAllRead() }
    }

    fun markAsRead(uri: AtUri) = serviceScope.launch {
        _notifications.update { it.markRead(uri) }
    }

    fun getUnreadCountLocal(): Long {
        return _notifications.value.notificationsList.count { !it.isRead }.toLong()
    }

    suspend fun getUnreadCount(): Result<Long> {
        return api.api.getUnreadCount(GetUnreadCountQuery(Clock.System.now())).map {
            log.d { "Unread count: ${it.count}" }
            it.count
        }
    }

    @OptIn(FlowPreview::class)
    suspend fun unreadCount(
        update: SharedFlow<Unit>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Flow<Long> = flow {
        update.debounce(300).collect {
            getUnreadCount().onSuccess {
                emit(it)
            }.onFailure {
                log.e { "Failed to get unread count: $it" }
                log.e { "Falling back to local" }
                emit(getUnreadCountLocal())
            }
        }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("UnreadCount"))

    suspend fun unreadNotifTick(interval: Long = 60000): SharedFlow<Unit> = flow {
        val updateTick = UpdateTick(interval)
        updateTick.tick(true)
        updateTick.t.collect {
            emit(Unit)
        }
    }.shareIn(serviceScope, SharingStarted.WhileSubscribed(), 1)

    fun unreadCountFlow(
        interval: Long = 60000,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Flow<Long> = flow {
        unreadCount(unreadNotifTick(interval), dispatcher).collect {
            emit(it)
        }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("UnreadCountFlow"))


    @OptIn(FlowPreview::class)
    suspend fun notifications(
        cursor: SharedFlow<AtCursor> = initAtCursor(),
        limit: Long = 50,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Flow<Result<NotificationsList>> = flow {
        cursor.debounce(300).collect { cursor ->
            val query = ListNotificationsQuery(limit, cursor)
            val result = api.api.listNotifications(query).map { response ->
                if(notifications.value.notificationsList.isNotEmpty()) {
                    if (cursor == null) {
                        NotificationsList(
                            response.notifications.mapImmutable { it.toBskyNotification() },
                            response.cursor
                        ).concat(notifications.value)
                    } else {
                        notifications.value.concat(response.notifications)
                    }
                } else {
                    NotificationsList(
                        response.notifications.mapImmutable { it.toBskyNotification() },
                        response.cursor
                    )
                }
            }
            if (result.isSuccess) {
                mutex.withLock {
                    _notifications.update { result.getOrThrow() }
                }
            } else log.e { "Failed to get notifications: ${result.exceptionOrNull()}" }
            emit(result)
        }
    }.distinctUntilChanged().flowOn(dispatcher + CoroutineName("Notifications"))
}

fun filterNotifications(
    list: ImmutableList<NotificationsListItem>,
    filter: NotificationsFilterState,
): ImmutableList<NotificationsListItem> {
    return list.filter {
        (if(it.isRead) filter.showAlreadyRead else true) &&
        when(it.reason) {
            ListNotificationsReason.LIKE -> filter.showLikes
            ListNotificationsReason.REPOST -> filter.showReposts
            ListNotificationsReason.FOLLOW -> filter.showFollows
            ListNotificationsReason.MENTION -> filter.showMentions
            ListNotificationsReason.REPLY -> filter.showReplies
            ListNotificationsReason.QUOTE -> filter.showQuotes
            else -> true
        }
    }.toImmutableList()
}