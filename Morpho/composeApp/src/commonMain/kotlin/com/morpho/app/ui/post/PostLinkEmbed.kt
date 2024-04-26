package com.morpho.app.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Size
import com.morpho.app.model.bluesky.BskyPostFeature
import com.morpho.app.ui.elements.RichTextElement
import com.morpho.app.ui.elements.WrappedColumn
import com.morpho.app.util.makeBlueskyText
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class)
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

        WrappedColumn() {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(linkData.thumb)
                    .size(Size.ORIGINAL)
                    .build(),
                contentDescription = linkData.uri.uri,
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.High,
                //placeholder = painterResource(Res.drawable.screenshot_20230924_200327),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable { linkPress(linkData.uri.uri) }
            )
            WrappedColumn(
                Modifier.clickable { linkPress(linkData.uri.uri) }
            ) {
                Text(
                    text = linkData.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
                val bskyTxt = remember { makeBlueskyText(linkData.description) }
                RichTextElement(
                    text = bskyTxt.text,
                    facets = bskyTxt.facets,
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