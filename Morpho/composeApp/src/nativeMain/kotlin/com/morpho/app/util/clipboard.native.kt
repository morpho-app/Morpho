package com.morpho.app.util

import org.koin.core.component.KoinComponent

actual object ClipboardManager: KoinComponent {
    actual fun copyToClipboard(text: String) {
    }
}