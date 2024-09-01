package com.morpho.app.util

import android.content.ClipData
import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.content.ClipboardManager as AndroidClipboardManager

actual object ClipboardManager: KoinComponent {
    private val context by inject<Context>()
    actual fun copyToClipboard(text: String) {
        val clipboard =  context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }
}