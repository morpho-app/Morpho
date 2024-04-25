package com.morpho.app.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.morpho.app.ui.elements.WrappedColumn

@Composable
actual fun TabbedScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    topContent: @Composable () -> Unit,
    modifier: Modifier,
)  {
    WrappedColumn {
        topContent()
        Scaffold(
            contentWindowInsets = WindowInsets.navigationBars,
            modifier = modifier,
            bottomBar = { navBar() },
            content = content
        )
    }
}