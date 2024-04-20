package com.morpho.app.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TabbedScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    topContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = modifier,
        topBar = { topContent() },
        bottomBar = { navBar() },
        content = content
    )
}