package com.morpho.app.util

import org.koin.core.component.KoinComponent

expect object ClipboardManager: KoinComponent {
    fun copyToClipboard(text: String)
}