package com.morpho.app.screens.main.tabbed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.bsky.actor.FeedType
import app.bsky.feed.GetPostThreadResponseThreadUnion
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.bluesky.toContentCardMapEntry
import com.morpho.app.model.bluesky.toThread
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uidata.ThreadUpdate
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging


class TabbedMainScreenModel : MainScreenModel() {

    val tabs = mutableStateListOf<ContentCardMapEntry>()

    val timelineIndex = agent.prefs.timelineIndex ?: agent.prefs.saved.indexOfFirst {
        it.type == FeedType.TIMELINE
    }.let { if(it == -1) 0 else it }

    var loaded by mutableStateOf(false)


    companion object {
        val log = logging("TabbedMainScreenModel")
    }

    init {
        screenModelScope.launch {
            while(!initialized) {
                delay(10)
            }
            feedSources.filter { it.pinned == true }.forEach {
                tabs.add(it.toContentCardMapEntry())
            }
            loaded = true

        }

    }

    fun uriForTab(index: Int): AtUri {
        return tabs[index].uri
    }

    fun getThread(uri: AtUri): Flow<ContentCardState.PostThread?> = flow {
        val post = getPost(uri).getOrNull()
        if(post != null) {
            val state = ContentCardState.PostThread(post)
            val thread = when(val thread = agent.getPostThread(uri,).getOrNull()?.thread) {
                is GetPostThreadResponseThreadUnion.ThreadViewPost -> thread.value.toThread()
                else -> null
            }
            if(thread != null) state.updates.emit(ThreadUpdate.Thread(thread))
            emit(state)
        }

    }


}