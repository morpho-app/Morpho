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

@Composable
actual fun TabbedScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    topContent: @Composable () -> Unit,
    modifier: Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = modifier,
        topBar = { topContent() },
        bottomBar = { navBar() },
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TabbedProfileScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    topContent: @Composable (TopAppBarScrollBehavior) -> Unit,
    modifier: Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    nestedScrollConnection: NestedScrollConnection,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = modifier,
        topBar = { topContent(scrollBehavior) },
        bottomBar = { navBar() },
        content = content
    )
}