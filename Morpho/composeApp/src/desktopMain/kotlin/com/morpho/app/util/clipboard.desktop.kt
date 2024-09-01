package com.morpho.app.util

import org.koin.core.component.KoinComponent
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual object ClipboardManager: KoinComponent {
    actual fun copyToClipboard(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
    }
}