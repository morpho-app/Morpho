package radiant.nimbus.ui.common

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.jeziellago.compose.markdowntext.MarkdownText
import radiant.nimbus.R
import radiant.nimbus.model.BskyPostFeature

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostLinkEmbed(
    linkData: BskyPostFeature.ExternalFeature
) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        tonalElevation = 3.dp,
        shadowElevation = 1.dp,
        //border = BorderStroke(1.dp,MaterialTheme.colorScheme.secondary)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val interactions = remember { mutableStateListOf<Interaction>() }
        val isPressed by interactionSource.collectIsPressedAsState()

        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        interactions.add(interaction)
                    }
                    is PressInteraction.Release -> {
                        interactions.remove(interaction.press)
                    }
                    is PressInteraction.Cancel -> {
                        interactions.remove(interaction.press)
                    }

                }
            }
        }

        val ctx = LocalContext.current
        LaunchedEffect(isPressed) {
            Log.d("tag","URL IS "+ linkData.uri.uri)
            val urlIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(linkData.uri.uri)
            )
            ctx.startActivity(urlIntent)
        }
        SelectionContainer {
            Column() {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(linkData.thumb)
                        .crossfade(true)
                        .build(),
                    contentDescription = linkData.uri.uri,
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(R.drawable.screenshot_20230924_200327),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .clip(MaterialTheme.shapes.extraSmall)
                )
                Text(
                    text = linkData.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )

                MarkdownText(
                    markdown = linkData.description.replace("\n", "  \n"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)
                )
            }
        }
    }
}


val testLinkEmbed = BskyPostFeature.ExternalFeature(
    uri = radiant.nimbus.api.Uri("https://www.youtube.com/watch?v=_q85LZqY5Ok"),
    title = "(Hearthstone) Big Ol' Tendies - Yogg Rogue",
    description = "\n" +
            "14,866 views  Sep 24, 2023\n" +
            "(TITANS Standard) 3 games: Yogg Tendril Rogue\n" +
            "Subscribe and turn on notifications for a new video every day!",
    thumb = "https://www.youtube.com/feed/subscriptions"

)