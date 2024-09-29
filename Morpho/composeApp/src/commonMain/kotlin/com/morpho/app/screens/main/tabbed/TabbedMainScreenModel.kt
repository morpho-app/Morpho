package com.morpho.app.screens.main.tabbed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.bsky.feed.GetPostThreadResponseThreadUnion
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.compose.LazyPagingItems
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.data.ContentLabelService
import com.morpho.app.data.MorphoAgent
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.bluesky.toContentCardMapEntry
import com.morpho.app.model.bluesky.toThread
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uidata.FeedPresenter
import com.morpho.app.model.uidata.ThreadUpdate
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.main.MainScreenModel
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication.Companion.init
import org.lighthousegames.logging.logging


class TabbedMainScreenModel(
    agent: MorphoAgent, labelService: ContentLabelService,
) : MainScreenModel(agent, labelService) {

    val tabs = mutableStateListOf<ContentCardMapEntry>()
    val tabPagers = mutableStateMapOf<AtUri, Flow<PagingData<MorphoDataItem>>>()

    val timelineIndex: Int
        get() = agent.prefs.timelineIndex ?: 0

    var loaded by mutableStateOf(false)


    companion object {
        val log = logging("TabbedMainScreenModel")
    }

    init {
        initializeTabs()
    }

    private fun initializeTabs() {
        screenModelScope.launch {
            while(!initialized) delay(10)
            feedSources.filter { it.pinned == true }.forEach { info ->
                tabs.add(info.toContentCardMapEntry())
                (feedPresenters[info.uri]?.pager?.flow?.cachedIn(screenModelScope)
                        as Flow<PagingData<MorphoDataItem>>).let {
                    tabPagers[info.uri] = it
                }

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

    override fun deinit() {
        super.deinit()
        tabPagers.clear()
        tabs.clear()
        loaded = false
    }


    override fun logout() {
        super.logout()
        deinit()
        initialize()
        initializeTabs()
    }

    override fun switchUser(did: Did) {
        super.switchUser(did)
        deinit()
        initialize()
        initializeTabs()
    }


}