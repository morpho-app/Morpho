package com.morpho.app.util

import java.awt.Desktop
import java.net.URI

actual fun openBrowser(url: String) {
    val desktop = Desktop.getDesktop()
    desktop.browse(URI.create(url))
}