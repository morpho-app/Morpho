package com.morpho.app.util


import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.UriHandler


actual fun openBrowser(url: String, uriHandler: UriHandler) {
    val urlIntent = Intent(
        Intent.ACTION_VIEW,
        safeUrlParse(url)
    )

    uriHandler.openUri(url)
}

fun safeUrlParse(uri: String): Uri? {
    val url = Uri.parse(uri)
    return if(url.scheme != "https" || url.scheme != "http") null else url
}