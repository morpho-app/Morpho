package com.morpho.app.util


import cafe.adriel.voyager.navigator.Navigator
import com.morpho.app.screens.base.tabbed.ProfileTab
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle

fun linkVisit(string: String, navigator: Navigator) {
    if(string.startsWith("@")) {
        if(string.startsWith("@did")) {
            navigator.push(ProfileTab(Did(string.removePrefix("@"))))
        } else {
            navigator.push(ProfileTab(Handle(string.removePrefix("@"))))
        }
    } else if(string.startsWith("https://bsky.app/")
        || string.startsWith("https://staging.bsky.app/")
    ) {
        if (string.contains("/post/")) {
            string.replace("/post/", "/app.bsky.feed.post/")
        }
    } else if (string.startsWith("http")){
        checkValidUrl(string)?.let { openBrowser(it) }
    }
}

expect fun openBrowser(url: String)

fun didCidToImageLink(did: Did, cid: Cid, avatar: Boolean, type: String = "jpeg"): String {
    val collection = if (avatar) {
        "avatar_thumbnail"
    } else {
        "feed_thumbnail"
    }
    return "https://cdn.bsky.app/img/$collection/plain/$did/$cid@$type"
}


fun parseImageThumbRef(string: String, did: Did): String {
    //Log.v("ImageThumb", string)
    var link = string.substringAfter("""{"${"$"}type":"blob","ref":{"${"$"}link":"""")
    link = link.substringBefore(""""},"mimeType":"image/jpeg","size""")
    return """https://cdn.bsky.app/img/feed_thumbnail/plain/$did/$link@jpeg"""
}

fun parseImageFullRef(string: String, did: Did): String {
    //Log.v("ImageFull", string)
    var link = string.substringAfter("""{"${"$"}type":"blob","ref":{"${"$"}link":"""")
    link = link.substringBefore(""""},"mimeType":"image/jpeg","size""")
    return """https://cdn.bsky.app/img/feed_fullsize/plain/$did/$link@jpeg"""
}

fun getRkey(uri: AtUri?) : String {
    val str = uri?.atUri.orEmpty()
    return str.substringAfterLast("/")
}

fun checkValidUrl(url: String): String? {
    return if (urlRegex.matches(url)) url else null
}