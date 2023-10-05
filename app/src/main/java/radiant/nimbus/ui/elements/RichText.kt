package radiant.nimbus.ui.elements

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import dev.jeziellago.compose.markdowntext.MarkdownText
import radiant.nimbus.model.DetailedProfile

@Composable
fun RichText(profile: DetailedProfile) {
    MarkdownText(
        // keeps stock bsky newline behaviour but allows fun formatting
        markdown = profile.description.orEmpty().replace("\n", "  \n"),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}