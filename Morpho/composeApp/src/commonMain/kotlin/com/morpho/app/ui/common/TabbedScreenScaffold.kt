package com.morpho.app.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun TabbedScreenScaffold(
    navBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    topContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
)