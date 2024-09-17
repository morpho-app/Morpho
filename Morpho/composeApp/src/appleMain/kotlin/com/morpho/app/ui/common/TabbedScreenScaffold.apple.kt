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
import com.morpho.app.model.uidata.Event
import com.morpho.app.model.uistate.ContentCardState

@Composable
actual fun <T> TabbedScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues, T?) -> Unit,
    topContent: @Composable () -> Unit,
    state: T?,
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
actual fun  <T: Event>  TabbedProfileScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues, ContentCardState<T>?) -> Unit,
    topContent: @Composable (TopAppBarScrollBehavior) -> Unit,
    state: ContentCardState<out T>?,
    modifier: Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    nestedScrollConnection: NestedScrollConnection,
) {
}