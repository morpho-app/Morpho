@file:Suppress("MemberVisibilityCanBePrivate")

package com.morpho.app.model.uistate

//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.butterfly.AtUri
import kotlinx.collections.immutable.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable


@Serializable
data class TabbedScreenState(
    override val loadingState: UiLoadingState = UiLoadingState.Idle,
    val tabs:  StateFlow<ImmutableList<ContentCardMapEntry>> =
        MutableStateFlow<ImmutableList<ContentCardMapEntry>>(persistentListOf()).asStateFlow(),
    val tabStates:  ImmutableList<StateFlow<ContentCardState<MorphoDataItem>>> = persistentListOf(),
): UiState {

    val tabMap: ImmutableMap<AtUri, ContentCardState<MorphoDataItem>>
        get() = tabStates.associateBy { it.value.uri }
            .filter { entry -> entry.value.value.uri in tabs.value.map { it.uri } }
            .mapValues { it.value.value }
            .toImmutableMap()
    val tabsWithNewPosts: ImmutableList<AtUri>
        get() = tabMap.filterValues { it.hasNewPosts }.keys.toImmutableList()

}



data class TabbedProfileScreenState(
    override val loadingState: UiLoadingState = UiLoadingState.Idle,
    val tabs: StateFlow<ImmutableList<ContentCardMapEntry>> =
        MutableStateFlow<ImmutableList<ContentCardMapEntry>>(persistentListOf()).asStateFlow(),
    val tabStates:  ImmutableList<StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>> = persistentListOf(),
): UiState {

    val tabMap: ImmutableMap<AtUri, ContentCardState.ProfileTimeline<MorphoDataItem>>
        get() = tabStates.associateBy { it.value.uri }
            .filter { entry -> entry.value.value.uri in tabs.value.map { it.uri } }
            .mapValues { it.value.value }
            .toImmutableMap()
    val tabsWithNewPosts: ImmutableList<AtUri>
        get() = tabMap.filterValues { it.hasNewPosts }.keys.toImmutableList()


}

