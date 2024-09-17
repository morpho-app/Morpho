package com.morpho.app.screens.base

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.paging.Pager
import app.cash.paging.cachedIn
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.NotificationsSource
import com.morpho.app.model.bluesky.toPost
import com.morpho.app.model.uidata.*
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

open class BaseScreenModel : ScreenModel, KoinComponent {
    val agent: MorphoAgent by inject()
    val labelService: ContentLabelService by inject()


    var userDid: Did? by mutableStateOf(agent.id)
        protected set

    val globalEvents = MutableSharedFlow<Event>(
        extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val isLoggedIn: Boolean
        get() = agent.isLoggedIn

    val notificationsRaw = Pager(NotificationsSource.defaultConfig) {
        NotificationsSource()
    }.flow.cachedIn(screenModelScope)



    companion object {
        val log = logging()
    }

    init {

    }

    suspend fun sendGlobalEvent(event: Event) {
        globalEvents.emit(event)
    }

    fun getProfilePresenter(
        id: Did,
        init: Boolean = false,
        eventStream: Flow<Event> = globalEvents
    ): Flow<Pair<ProfilePresenter, MutableStateFlow<UIUpdate>>> = flow {
        val presenter = ProfilePresenter.create(agent, id)?: return@flow
        if(!init) emit(Pair(presenter, MutableStateFlow(UIUpdate.Empty)))
        else {
            val stateFlow = MutableStateFlow<UIUpdate>(UIUpdate.Empty)
            emit(Pair(presenter, stateFlow))
            screenModelScope.launch {
                stateFlow.emitAll(presenter.produceUpdates(eventStream))
            }
        }
    }

    fun getMyProfilePresenter(
        init: Boolean = false,
        eventStream: Flow<Event> = globalEvents
    ): Flow<Pair<MyProfilePresenter, MutableStateFlow<UIUpdate>>> = flow {
        val presenter = MyProfilePresenter.create(agent)?: return@flow
        if(!init) emit(Pair(presenter, MutableStateFlow(UIUpdate.Empty)))
        else {
            val stateFlow = MutableStateFlow<UIUpdate>(UIUpdate.Empty)
            emit(Pair(presenter, stateFlow))
            screenModelScope.launch {
                stateFlow.emitAll(presenter.produceUpdates(eventStream))
            }
        }
    }

    fun unreadNotificationsCount() = flow {
        emit(agent.unreadNotificationsCount().getOrDefault(0))
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), 0L)

    fun markNotificationsAsRead() = screenModelScope.launch {
        agent.updateSeenNotifications()
        globalEvents.emit(Event.UpdateSeenNotifications())
    }

    suspend fun getPost(uri: AtUri): Result<BskyPost> {
        return agent.getPosts(listOf(uri)).map {
            if(it.isEmpty()) Result.failure(Exception("Post not found"))
            else Result.success(it.first().toPost())
        }.getOrDefault(Result.failure(Exception("Post not found")))
    }


}