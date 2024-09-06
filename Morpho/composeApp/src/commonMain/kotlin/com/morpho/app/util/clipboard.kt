@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.morpho.app.util

import org.koin.core.component.KoinComponent

expect object ClipboardManager: KoinComponent {
    fun copyToClipboard(text: String)
}