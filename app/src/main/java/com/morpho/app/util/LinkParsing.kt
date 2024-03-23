package com.morpho.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import com.morpho.app.screens.destinations.ProfileScreenDestination
import com.morpho.butterfly.Handle

fun linkVisit(string: String, ctx: Context, navigator: DestinationsNavigator) {
    if(string.startsWith("@")) {
        if(string.startsWith("@did")) {
            navigator.navigate(ProfileScreenDestination(Did(string.removePrefix("@"))))
        } else {
            navigator.navigate(ProfileScreenDestination(Handle(string.removePrefix("@"))))
        }

    } else if(string.startsWith("https://bsky.app/")
        || string.startsWith("https://staging.bsky.app/")
    ) {
        if (string.contains("/post/")) {
            string.replace("/post/", "/app.bsky.feed.post/")
        } else {

        }

    } else if (string.startsWith("http")){
        val urlIntent = Intent(
            Intent.ACTION_VIEW,
            safeUrlParse(string)
        )
        ctx.startActivity(urlIntent)
    }

}

fun parseImageThumbRef(string: String, did: Did): String {
    Log.v("ImageThumb", string)
    var link = string.substringAfter("""{"${"$"}type":"blob","ref":{"${"$"}link":"""")
    link = link.substringBefore(""""},"mimeType":"image/jpeg","size""")
    return """https://cdn.bsky.app/img/feed_thumbnail/plain/$did/$link@jpeg"""
}

fun parseImageFullRef(string: String, did: Did): String {
    Log.v("ImageFull", string)
    var link = string.substringAfter("""{"${"$"}type":"blob","ref":{"${"$"}link":"""")
    link = link.substringBefore(""""},"mimeType":"image/jpeg","size""")
    return """https://cdn.bsky.app/img/feed_fullsize/plain/$did/$link@jpeg"""
}

fun getRkey(uri: AtUri?) : String {
    val str = uri?.atUri.orEmpty()
    return str.substringAfterLast("/")
}

fun safeUrlParse(uri: String): Uri? {
    val url = Uri.parse(uri)
    if(url.scheme != "https" || url.scheme != "http") return null else return url
}