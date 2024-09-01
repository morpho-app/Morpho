package com.morpho.app.util


import android.content.Intent
import android.net.Uri
import com.morpho.app.MorphoApplication

actual fun openBrowser(url: String) {
    val urlIntent = Intent(
        Intent.ACTION_VIEW,
        safeUrlParse(url)
    )
    MorphoApplication().applicationContext.startActivity(urlIntent)
}

fun safeUrlParse(uri: String): Uri? {
    val url = Uri.parse(uri)
    return if(url.scheme != "https" || url.scheme != "http") null else url
}