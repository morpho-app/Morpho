package com.morpho.app.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uistate.ContentCardState
import kotlinx.coroutines.flow.StateFlow

@Composable
actual fun <T> TabbedScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues, StateFlow<T>?) -> Unit,
    topContent: @Composable () -> Unit,
    state: StateFlow<T>?,
    modifier: Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = modifier,
        topBar = { topContent() },
        bottomBar = { navBar() },
        content = { insets ->
            content(insets, state)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TabbedProfileScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues, StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>?) -> Unit,
    topContent: @Composable (TopAppBarScrollBehavior) -> Unit,
    state: StateFlow<ContentCardState.ProfileTimeline<MorphoDataItem>>?,
    modifier: Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    nestedScrollConnection: NestedScrollConnection,
) {
}