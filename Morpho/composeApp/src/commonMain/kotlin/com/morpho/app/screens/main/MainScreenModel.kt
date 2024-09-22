package com.morpho.app.screens.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.bsky.actor.SavedFeed
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.bluesky.FeedSourceInfo
import com.morpho.app.model.bluesky.toFeedSourceInfo
import com.morpho.app.model.bluesky.toProfile
import com.morpho.app.model.uidata.FeedEvent
import com.morpho.app.model.uidata.FeedPresenter
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.screens.base.BaseScreenModel
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging


open class MainScreenModel: BaseScreenModel() {

    var userProfile: DetailedProfile? by mutableStateOf(null)
        protected set

    val feedSources = mutableStateListOf<FeedSourceInfo>()
    val feedPresenters = mutableMapOf<AtUri, FeedPresenter<FeedEvent>>()
    val pinnedFeeds: List<SavedFeed>
        get() = agent.prefs.saved.filter { it.pinned }


    val feedStates = mutableMapOf<AtUri, ContentCardState<FeedEvent>>()

    var initialized = false

    companion object {
        val log = logging("MainScreenModel")
    }

    init {
        if(isLoggedIn) screenModelScope.launch {
            userProfile = userDid?.let { agent.getProfile(it).getOrNull()?.toProfile() }
            feedSources.addAll(pinnedFeeds.mapNotNull { feed -> feed.toFeedSourceInfo(agent).getOrNull() })
            feedPresenters.putAll(feedSources.map { source ->
                source.uri to FeedPresenter(source.feedDescriptor)
            })
            feedStates.putAll(feedSources.map { source ->
                source.uri to  ContentCardState.Skyline(source.uri)
            })


            screenModelScope.launch {
                for(source in feedSources) {
                    val cardState = feedStates[source.uri]?: continue
                    val presenter = feedPresenters[source.uri] ?: continue
                    screenModelScope.launch {
                        cardState.updates.emitAll(
                            presenter.produceUpdates(merge(globalEvents, cardState.events))
                        )
                    }
                }

            }
            initialized = true


        }
    }




}