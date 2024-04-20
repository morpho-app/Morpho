package com.morpho.app.ui.common

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(content: () -> Unit) {
    BackHandler {
        content()
    }
}