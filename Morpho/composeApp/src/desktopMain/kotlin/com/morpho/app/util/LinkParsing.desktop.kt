package com.morpho.app.util

import java.awt.Desktop
import java.net.URI
import java.util.Locale


actual fun openBrowser(url: String) {
    val osName by lazy(LazyThreadSafetyMode.NONE) { System.getProperty("os.name").lowercase(Locale.ROOT) }
    val desktop = Desktop.getDesktop()
    try {
        when {
            Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE) -> {
                desktop.browse(URI.create(url))
            }
            "mac" in osName -> {
                Runtime.getRuntime().exec("open $url")
            }
            "nix" in osName || "nux" in osName -> Runtime.getRuntime().exec("xdg-open $url")
            else -> throw RuntimeException("cannot open $url")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

