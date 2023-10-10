package radiant.nimbus.ui.elements

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.jeziellago.compose.markdowntext.MarkdownText
import radiant.nimbus.model.DetailedProfile

@Composable
fun RichText(profile: DetailedProfile) {
    val ctx = LocalContext.current
    MarkdownText(
        // keeps stock bsky newline behaviour but allows fun formatting
        markdown = profile.description.orEmpty().replace("\n", "  \n"),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        disableLinkMovementMethod = true,
        onLinkClicked = {
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(it)
            )
            ctx.startActivity(urlIntent)
        },
    )
}