package com.morpho.app.screens.base

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.paging.Pager
import app.cash.paging.cachedIn
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.data.ContentLabelService
import com.morpho.app.data.MorphoAgent
import com.morpho.app.di.UpdateTick
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.bluesky.NotificationsSource
import com.morpho.app.model.bluesky.toPost
import com.morpho.app.model.bluesky.toProfile
import com.morpho.app.model.uidata.Event
import com.morpho.app.model.uidata.MyProfilePresenter
import com.morpho.app.model.uidata.ProfilePresenter
import com.morpho.app.model.uidata.UIUpdate
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

open class BaseScreenModel(
    val agent: MorphoAgent,
    val labelService: ContentLabelService
) : ScreenModel {
    //val agent: MorphoAgent by inject()
    //val labelService: ContentLabelService by inject()

    var userProfile: DetailedProfile? by mutableStateOf(null)
        protected set

    val kawaiiMode: Boolean
        get() = agent.kawaiiMode

    var userDid: Did? by mutableStateOf(agent.id)
        protected set

    val globalEvents = MutableSharedFlow<Event>(
        extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val isLoggedIn = MutableStateFlow(agent.isLoggedIn)

    val notifications = Pager(NotificationsSource.defaultConfig) {
        NotificationsSource()
    }.flow.cachedIn(screenModelScope)

    var notifJob: Job? = null

    companion object {
        val log = logging()
    }

    private val notificationsTick = UpdateTick(10000)
    init {
        screenModelScope.launch {
            if(!agent.isLoggedIn) {
                while(!agent.isLoggedIn) {
                    delay(100)
                    isLoggedIn.value = agent.isLoggedIn
                }
            }
        }
        screenModelScope.launch {
            while(!isLoggedIn.value) delay(10)
            userProfile = userDid?.let { agent.getProfile(it).getOrNull()?.toProfile() }
        }
        notifJob = screenModelScope.launch {
            notificationsTick.tick(true)
        }
    }

    fun sendGlobalEvent(event: Event) {
        globalEvents.tryEmit(event)
    }

    open fun logout() {
        agent.logout().invokeOnCompletion {
            deinit()
            isLoggedIn.value = false
            userDid = null
            userProfile = null

        }
    }

    open fun switchUser(did: Did) {
        screenModelScope.launch {
            deinit()
            agent.switchUser(did)
            userDid = did
            userProfile = agent.getProfile(did).getOrNull()?.toProfile()
            notifJob = screenModelScope.launch {
                notificationsTick.tick(true)
            }
        }

    }

    fun getProfilePresenter(
        id: Did,
        init: Boolean = false,
        eventStream: Flow<Event> = globalEvents
    ): Flow<Pair<ProfilePresenter, MutableStateFlow<UIUpdate>>> = flow {
        val profile = agent.getProfile(id).getOrNull()?.toProfile()
        val presenter = ProfilePresenter.create(agent, id, profile)?: return@flow
        if(!init) emit(Pair(presenter, MutableStateFlow(UIUpdate.Empty)))
        else {
            val stateFlow = MutableStateFlow<UIUpdate>(UIUpdate.Empty)
            screenModelScope.launch {
                stateFlow.emitAll(presenter.produceUpdates(eventStream))
            }
            emit(Pair(presenter, stateFlow))
        }
    }.distinctUntilChanged() as Flow<Pair<ProfilePresenter, MutableStateFlow<UIUpdate>>>

    fun getMyProfilePresenter(
        init: Boolean = false,
        eventStream: Flow<Event> = globalEvents
    ): Flow<Pair<MyProfilePresenter, MutableStateFlow<UIUpdate>>> = flow {
        val presenter = MyProfilePresenter.create(agent, userProfile)?: return@flow
        if(!init) emit(Pair(presenter, MutableStateFlow(UIUpdate.Empty)))
        else {
            val stateFlow = MutableStateFlow<UIUpdate>(UIUpdate.Empty)
            screenModelScope.launch {
                stateFlow.emitAll(presenter.produceUpdates(eventStream))
            }
            emit(Pair(presenter, stateFlow))

        }
    }.distinctUntilChanged() as Flow<Pair<MyProfilePresenter, MutableStateFlow<UIUpdate>>>



    fun unreadNotificationsCount() = notificationsTick.t.map {
        agent.unreadNotificationsCount().getOrDefault(0)
    }.distinctUntilChanged()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(), 0L)

    fun updateSeenNotifications() = screenModelScope.launch {
        agent.updateSeenNotifications()
        globalEvents.emit(Event.UpdateSeenNotifications())
    }

    suspend fun getPost(uri: AtUri): Result<BskyPost> {
        return agent.getPosts(listOf(uri)).map {
            if(it.isEmpty()) Result.failure(Exception("Post not found"))
            else Result.success(it.first().toPost())
        }.getOrDefault(Result.failure(Exception("Post not found")))
    }

    open fun deinit() {
        notifJob?.cancel()
    }


}