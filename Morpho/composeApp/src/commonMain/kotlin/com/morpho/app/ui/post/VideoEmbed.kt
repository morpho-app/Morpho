package com.morpho.app.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayDisabled
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.bsky.embed.AspectRatio
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Size
import com.morpho.app.model.bluesky.EmbedVideo
import com.morpho.app.model.bluesky.EmbedVideoView
import com.morpho.app.model.bluesky.VideoEmbed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoEmbedThumb(
    video: VideoEmbed,
    alt: String = "",
    aspectRatio: AspectRatio? = null,
    modifier: Modifier = Modifier
        .padding(vertical = 6.dp)
        .heightIn(10.dp, 700.dp)
        .fillMaxWidth(),
) {

    val showAltText = remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = modifier.padding(2.dp)
    ) {
        if (aspectRatio == null) {
            when(video) {
                is EmbedVideo -> {
                }
                is EmbedVideoView -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(video.thumbnail.atUri)
                            .build(),
                        contentDescription = alt,
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)


                    )
                    IconButton(
                        onClick = { },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayDisabled,
                            contentDescription = "Video not quite supported yet",
                            modifier = Modifier
                                .size(50.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }
        } else {
            var width = with(LocalDensity.current) { maxWidth.value.dp.toPx() }
            var height = with(LocalDensity.current) { maxHeight.value.dp.toPx() }
            val ratio = aspectRatio.width.toFloat() / aspectRatio.height.toFloat()
            if (ratio > 1) {
                height /= ratio
            } else {
                width /= ratio
            }
            when(video) {
                is EmbedVideo -> {
                }
                is EmbedVideoView -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(video.thumbnail.atUri)
                            .size(Size(width.toInt(), height.toInt()))
                            .build(),
                        contentDescription = alt,
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)

                    )
                    IconButton(
                        onClick = { },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayDisabled,
                            contentDescription = "Video not quite supported yet",
                            modifier = Modifier
                                .size(50.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }

        }

        if (alt.isNotEmpty()) {
            when(showAltText.value) {
                true -> {

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAltText.value = false }
                            .align(Alignment.BottomStart)
                    ){
                        SelectionContainer {
                            Text(
                                text = alt,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(6.dp),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
                false -> {
                    DisableSelection {
                        TextButton(
                            onClick = {showAltText.value = true},
                            border = null,
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.8f),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(0.dp),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .requiredHeightIn(25.dp, 30.dp)
                                .requiredWidthIn(40.dp, 70.dp)
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Text(
                                text = "ALT",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize.times(0.9),
                                modifier = Modifier
                                    .padding(0.dp)
                                    .verticalScroll(
                                        rememberScrollState()
                                    ),
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                }
            }

        }
    }

}