package morpho.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import morpho.app.api.AtIdentifier
import morpho.app.api.AtUri
import morpho.app.api.Did
import morpho.app.screens.destinations.ProfileScreenDestination

fun linkVisit(string: String, ctx: Context, navigator: DestinationsNavigator) {
    if(string.startsWith("@")) {
        navigator.navigate(ProfileScreenDestination(AtIdentifier(string.removePrefix("@"))))
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
            Uri.parse(string)
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