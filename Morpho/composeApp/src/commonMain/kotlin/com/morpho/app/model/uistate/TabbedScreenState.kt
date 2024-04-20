@file:Suppress("MemberVisibilityCanBePrivate")

package com.morpho.app.model.uistate

import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.butterfly.AtUri
import kotlinx.collections.immutable.*
//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.serialization.Serializable


@Serializable
data class TabbedScreenState(
    override val loadingState: UiLoadingState = UiLoadingState.Idle,
    val selectedTabIndex: Int = 0,
    val tabs: ImmutableList<ContentCardMapEntry> = persistentListOf(),
    private val tabStates: ImmutableList<ContentCardState<MorphoDataItem.FeedItem>> = persistentListOf(),
): UiState {
    val tabMap: ImmutableMap<AtUri, ContentCardState<MorphoDataItem.FeedItem>>
        get() = tabStates.associateBy { it.uri }
            .filter { entry -> entry.value.uri in tabs.map { it.uri } }.toPersistentMap()
    val tabsWithNewPosts: ImmutableList<AtUri>
        get() = tabMap.filterValues { it.hasNewPosts }.keys.toImmutableList()

    val selectedTabState: ContentCardState<MorphoDataItem.FeedItem>?
        // If we could guarantee the same sorting,
        // this could be simplified to tabStates.getOrNull(selectedTabIndex)
        get() = tabMap[tabs[selectedTabIndex].uri]

    val selectedTab: ContentCardMapEntry?
        get() = tabs.getOrNull(selectedTabIndex)
}

data class TabbedProfileScreenState(
    override val loadingState: UiLoadingState = UiLoadingState.Idle,
    val selectedTabIndex: Int = 0,
    val tabs: ImmutableList<ContentCardMapEntry> = persistentListOf(),
    private val tabStates: ImmutableList<ContentCardState.ProfileTimeline<MorphoDataItem>> = persistentListOf(),
): UiState {
    val tabMap: ImmutableMap<AtUri, ContentCardState.ProfileTimeline<MorphoDataItem>>
        get() = tabStates.associateBy { it.uri }
            .filter { entry -> entry.value.uri in tabs.map { it.uri } }.toPersistentMap()
    val tabsWithNewPosts: ImmutableList<AtUri>
        get() = tabMap.filterValues { it.hasNewPosts }.keys.toImmutableList()

    val selectedTabState: ContentCardState.ProfileTimeline<MorphoDataItem>?
        // If we could guarantee the same sorting,
        // this could be simplified to tabStates.getOrNull(selectedTabIndex)
        get() = tabMap[tabs[selectedTabIndex].uri]

    val selectedTab: ContentCardMapEntry?
        get() = tabs.getOrNull(selectedTabIndex)
}
