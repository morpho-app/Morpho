@file:Suppress("MemberVisibilityCanBePrivate")

package com.morpho.app.model.uistate

//import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import androidx.compose.runtime.Immutable
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.util.StateFlowSerializer
import com.morpho.butterfly.AtUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable


@Immutable
@Serializable
data class TabbedScreenState(
    override val loadingState: UiLoadingState = UiLoadingState.Idle,
    @Serializable(with = StateFlowSerializer::class)
    val tabs:  StateFlow<List<ContentCardMapEntry>> =
        MutableStateFlow<List<ContentCardMapEntry>>(listOf()).asStateFlow(),
    val tabStates:  List<StateFlow<ContentCardState<MorphoDataItem>>> = listOf(),
): UiState {

    val tabMap: Map<AtUri, ContentCardState<MorphoDataItem>>
        get() = tabStates.associateBy { it.value.uri }
            .filter { entry -> entry.value.value.uri in tabs.value.map { it.uri } }
            .mapValues { it.value.value }.toMap()
    val tabsWithNewPosts: List<AtUri>
        get() = tabMap.filterValues { it.hasNewPosts }.keys.toList()

}



@Immutable
@Serializable
data class TabbedProfileScreenState(
    override val loadingState: UiLoadingState = UiLoadingState.Idle,
    @Serializable(with = StateFlowSerializer::class)
    val tabs: StateFlow<List<ContentCardMapEntry>> =
        MutableStateFlow<List<ContentCardMapEntry>>(listOf()).asStateFlow(),
    val tabStates:  List<StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>> = listOf(),
): UiState {

    val tabMap: Map<AtUri, ContentCardState.ProfileTimeline<MorphoDataItem>>
        get() = tabStates.associateBy { it.value.uri }
            .filter { entry -> entry.value.value.uri in tabs.value.map { it.uri } }
            .mapValues { it.value.value }.toMap()
    val tabsWithNewPosts: List<AtUri>
        get() = tabMap.filterValues { it.hasNewPosts }.keys.toList()


}

