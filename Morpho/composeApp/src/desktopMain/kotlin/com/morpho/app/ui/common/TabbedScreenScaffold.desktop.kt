package com.morpho.app.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.model.uidata.Event
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.ui.elements.WrappedColumn

@Composable
actual fun <T> TabbedScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues, T?) -> Unit,
    topContent: @Composable () -> Unit,
    state: T?,
    modifier: Modifier,
    drawerState: DrawerState,
    profile: DetailedProfile?
)  {
    NavDrawer(
        drawerState = drawerState,
        profile = profile,
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.navigationBars,
            modifier = modifier,
            bottomBar = { navBar() },
            content = {
                WrappedColumn(
                    modifier = modifier
                ) {
                    topContent()
                    content(it, state)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun <T: Event> TabbedProfileScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues, ContentCardState<out T>?) -> Unit,
    topContent: @Composable (TopAppBarScrollBehavior) -> Unit,
    state: ContentCardState<out T>?,
    modifier: Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    nestedScrollConnection: NestedScrollConnection,
    drawerState: DrawerState,
    profile: DetailedProfile?
) {
    NavDrawer(
        drawerState = drawerState,
        profile = profile,
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.navigationBars,
            modifier = modifier,
            bottomBar = { navBar() },
            content = {
                WrappedColumn(
                    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                ) {
                    topContent(scrollBehavior)
                    content(it, state)
                }
            }
        )
    }
}