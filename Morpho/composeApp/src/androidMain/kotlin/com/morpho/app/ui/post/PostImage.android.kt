package com.morpho.app.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.morpho.app.model.bluesky.EmbedImage
import com.morpho.app.ui.common.BackHandler

@Composable
actual fun FullImageView(
    image: EmbedImage,
    modifier: Modifier,
    onDismissRequest: () -> Unit,
) {
    val hasAltText = remember { image.alt.isNotEmpty() }
    val showAltText = remember{ mutableStateOf(true) }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true,)
    ) {
        BackHandler {
            onDismissRequest()
        }
        Box(
            Modifier.systemBarsPadding()
                .fillMaxSize()
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .clickable { onDismissRequest() }
                .align(Alignment.Center)
            ) {
                IconButton(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.align(Alignment.End)//.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Return to Post")
                }
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(image.fullsize)
                        .crossfade(true)
                        .build(),
                    contentDescription = image.alt,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAltText.value = !showAltText.value }
                )

                if (hasAltText && showAltText.value) {

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        SelectionContainer(
                            Modifier.clickable { }
                        ) {
                            Text(
                                text = image.alt,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}