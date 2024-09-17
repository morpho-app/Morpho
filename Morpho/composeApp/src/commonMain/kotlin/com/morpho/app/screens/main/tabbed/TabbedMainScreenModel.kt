package com.morpho.app.screens.main.tabbed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.bsky.actor.FeedType
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.bluesky.toContentCardMapEntry
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging


class TabbedMainScreenModel : MainScreenModel() {

    val tabs = mutableStateListOf<ContentCardMapEntry>()

    val timelineIndex = agent.prefs.timelineIndex ?: agent.prefs.saved.indexOfFirst {
        it.type == FeedType.TIMELINE
    }.let { if(it == -1) 0 else it }
    val lastPinnedIndex = agent.prefs.saved.indexOfLast { it.pinned }

    var loaded by mutableStateOf(false)


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
                tabs.add(source.toContentCardMapEntry())
            }

        }

    }

    fun uriForTab(index: Int): AtUri {
        return tabs[index].uri
    }


}