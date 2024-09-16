package com.morpho.app.screens.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.bsky.actor.SavedFeed
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.bluesky.*
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.model.uidata.FeedEvent
import com.morpho.app.model.uidata.FeedPresenter
import com.morpho.app.model.uidata.FeedUpdate
import com.morpho.app.screens.base.BaseScreenModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging


open class MainScreenModel: BaseScreenModel() {

    var userProfile: DetailedProfile? by mutableStateOf(null)
        protected set

    val feedSources = mutableStateListOf<FeedSourceInfo>()
    val feedPresenters = mutableMapOf<FeedSourceInfo, FeedPresenter<MorphoDataItem.FeedItem, FeedEvent>>()
    val pinnedFeeds: List<SavedFeed>
        get() = agent.prefs.saved.filter { it.pinned }


    val feedStates = mutableMapOf<FeedSourceInfo, ContentCardMapEntry<FeedEvent>>()

    companion object {
        val log = logging("MainScreenModel")
    }

    init {
        if(isLoggedIn) screenModelScope.launch {
            userProfile = userDid?.let { agent.getProfile(it).getOrNull()?.toProfile() }
            feedSources.add(FeedSourceInfo.Home)
            feedSources.addAll(pinnedFeeds.mapNotNull { feed -> feed.toFeedSourceInfo(agent).getOrNull() })
            feedPresenters.putAll(feedSources.map { source ->
                source to FeedPresenter(source.feedDescriptor)
            })
            feedStates.putAll(feedSources.map { source ->
                source to ContentCardMapEntry.Feed(
                    source.uri, source.displayName?:"",
                    events = MutableSharedFlow(
                        extraBufferCapacity = 10,
                        onBufferOverflow = BufferOverflow.DROP_OLDEST),
                    updates = MutableStateFlow(FeedUpdate.Empty))
            })


            screenModelScope.launch {
                feedPresenters.forEach { (source, presenter) ->
                    val entry = feedStates[source]?: return@forEach
                    entry.updates.emitAll(
                        presenter.produceUpdates(entry.events.filterIsInstance<FeedEvent>())
                    )
                }
            }

        }
    }




}