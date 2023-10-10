package radiant.nimbus.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.screens.destinations.ProfileScreenDestination

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

fun getRkey(uri: AtUri?) : String {
    val str = uri?.atUri.orEmpty()
    return str.substringAfterLast("/")
}