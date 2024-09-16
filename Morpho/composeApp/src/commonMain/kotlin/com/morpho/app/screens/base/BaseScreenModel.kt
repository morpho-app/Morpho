package com.morpho.app.screens.base

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.uidata.BskyNotificationService
import com.morpho.app.model.uidata.ContentLabelService
import com.morpho.app.model.uidata.Event
import com.morpho.butterfly.Did
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

open class BaseScreenModel : ScreenModel, KoinComponent {
    val agent: MorphoAgent by inject()
    val notifService: BskyNotificationService by inject()
    val labelService: ContentLabelService by inject()


    var userDid: Did? by mutableStateOf(agent.id)
        protected set

    val globalEvents = MutableSharedFlow<Event>(
        extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val isLoggedIn: Boolean
        get() = agent.isLoggedIn

    companion object {
        val log = logging()
    }

    init {

    }

    suspend fun sendGlobalEvent(event: Event) {
        globalEvents.emit(event)
    }
}