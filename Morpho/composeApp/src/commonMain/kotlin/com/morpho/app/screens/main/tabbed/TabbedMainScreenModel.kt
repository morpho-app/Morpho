package com.morpho.app.screens.main.tabbed

import app.bsky.actor.FeedType
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uidata.FeedEvent
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging


class TabbedMainScreenModel : MainScreenModel() {

    private val _tabs = mutableListOf<ContentCardMapEntry<FeedEvent>>()
    val tabs: List<ContentCardMapEntry<FeedEvent>>
        get() = _tabs.toList()

    val timelineIndex = agent.prefs.timelineIndex ?: agent.prefs.saved.indexOfFirst {
        it.type == FeedType.TIMELINE
    }.let { if(it == -1) 0 else it }
    val lastPinnedIndex = agent.prefs.saved.indexOfLast { it.pinned }

    companion object {
        val log = logging("TabbedMainScreenModel")
    }

    init {
        screenModelScope.launch {
            while(!initialized) {
                delay(10)
            }
            for(i in 0 ..  lastPinnedIndex) {
                val source = feedSources[i]
                feedStates[source]?.let { _tabs.add(it) }
            }

        }

    }

    fun uriForTab(index: Int): AtUri {
        return tabs[index].uri
    }


}