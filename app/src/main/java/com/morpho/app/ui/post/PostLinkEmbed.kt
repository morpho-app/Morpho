package com.morpho.app.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.morpho.app.model.BskyPostFeature

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostLinkEmbed(
    linkData: BskyPostFeature.ExternalFeature,
    linkPress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {


    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        tonalElevation = 3.dp,
        shadowElevation = 1.dp,
        modifier = modifier
        //border = BorderStroke(1.dp,MaterialTheme.colorScheme.secondary)
    ) {

        Column(Modifier.clickable { }) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(linkData.thumb)
                    .size(Size.ORIGINAL)
                    .build(),
                contentDescription = linkData.uri.uri,
                contentScale = ContentScale.Fit,
                //placeholder = painterResource(R.drawable.screenshot_20230924_200327),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable { linkPress(linkData.uri.uri) }
            )
            SelectionContainer(
                Modifier
                    .clickable { linkPress(linkData.uri.uri) }
            ) {
                Text(
                    text = linkData.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
            SelectionContainer(
                Modifier.clickable { linkPress(linkData.uri.uri) }
            ) {
                MarkdownText(
                    markdown = linkData.description.replace("\n", "  \n"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    linkColor = MaterialTheme.colorScheme.tertiary,
                    onLinkClicked = {
                        linkPress(it)
                    },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)
                )
            }
        }

    }
}


val testLinkEmbed = BskyPostFeature.ExternalFeature(
    uri = com.morpho.butterfly.Uri("https://www.youtube.com/watch?v=_q85LZqY5Ok"),
    title = "(Hearthstone) Big Ol' Tendies - Yogg Rogue",
    description = "\n" +
            "14,866 views  Sep 24, 2023\n" +
            "(TITANS Standard) 3 games: Yogg Tendril Rogue\n" +
            "Subscribe and turn on notifications for a new video every day!",
    thumb = "https://www.youtube.com/feed/subscriptions"

)