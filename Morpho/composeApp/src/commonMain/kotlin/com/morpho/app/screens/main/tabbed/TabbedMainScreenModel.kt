package com.morpho.app.screens.main.tabbed

import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uidata.Event
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.lighthousegames.logging.logging

@Suppress("UNCHECKED_CAST")
@Serializable
class TabbedMainScreenModel : MainScreenModel() {

    @Contextual private val tabs = mutableListOf<ContentCardMapEntry<Event>>()


    companion object {
        val log = logging("TabbedMainScreenModel")
    }

    init {
        if(isLoggedIn) screenModelScope.launch {

        }
    }

    fun uriForTab(index: Int): AtUri {
        return tabs[index].uri
    }


}