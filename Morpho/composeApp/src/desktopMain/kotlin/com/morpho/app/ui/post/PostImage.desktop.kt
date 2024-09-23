package com.morpho.app.ui.post

import MorphoDialog
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.morpho.app.data.PreferencesRepository
import com.morpho.app.model.bluesky.EmbedImage
import com.morpho.app.ui.elements.WrappedColumn
import com.morpho.app.ui.theme.MorphoTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.getKoin
import org.lighthousegames.logging.logging

val log = logging("imageview")

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun FullImageView(
    image: EmbedImage,
    modifier: Modifier,
    onDismissRequest: () -> Unit,
) {
    val prefs = getKoin().get<PreferencesRepository>()
    val morphoPrefs = runBlocking {
        prefs.prefs.firstOrNull()?.firstOrNull()?.morphoPrefs
    }
    val (undecorated, tabbed) = if (morphoPrefs != null) {
        log.d{ "Morpho Preferences: $morphoPrefs" }
        (morphoPrefs.tabbed == true) to (morphoPrefs.undecorated == true)
    } else {
        log.d {"No Morpho Preferences found, using defaults" }
        true to true
    }
    val (width, height) = 1000.dp to 1100.dp

    val ratio = remember {
        if (image.aspectRatio != null)
            image.aspectRatio.width.toFloat() / image.aspectRatio.height.toFloat()
        else null
    }

    val state = rememberDialogState(
        width = (if(ratio != null && ratio > 1) { 1000.dp * ratio } else if(ratio != null) { 1000.dp } else 1000.dp),
        height = (
                (if(ratio != null && ratio > 1) { 1100.dp } else if(ratio != null) { 1100.dp * ratio } else 1100.dp)
                        //+ (100.dp + 40.dp * image.alt.lines().size)
                )
    )


    DialogWindow(
        title = "Image: ${image.alt}",
        onCloseRequest = onDismissRequest,
        undecorated = undecorated,
        transparent = undecorated,
        state = state,
    ) {
        MorphoTheme(darkTheme = isSystemInDarkTheme()) {
            if(undecorated) {
                MorphoDialog(
                    title = "Image: ${image.alt}",
                    windowState = state,
                    onCloseRequest = onDismissRequest
                ) {
                    DesktopImageViewContent(
                        image = image,
                        onDismissRequest = onDismissRequest
                    )
//                    println("Image width: ${image.aspectRatio?.width} Image height: ${image.aspectRatio?.height} Ratio: $ratio")
//                    println("Width: ${state.size.width.value.toInt()} Height: ${state.size.height.value.toInt()} Ratio: ${
//                        state.size.width.value.toInt().toFloat() / state.size.height.value.toInt().toFloat()
//                    }")
                }
            } else {
                DesktopImageViewContent(
                    image = image,
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }
}

@Composable
fun DesktopImageViewContent(
    image: EmbedImage,
    onDismissRequest: () -> Unit,
) {
    val hasAltText = remember { image.alt.isNotEmpty() }
    val vScrollState = rememberScrollState()
    val hScrollState = rememberScrollState()

    val scroll = if(image.aspectRatio == null){
        Modifier.verticalScroll(vScrollState).horizontalScroll(hScrollState)
    } else {
        Modifier
    }
    val scale = if(image.aspectRatio == null) {
        Modifier.wrapContentSize()
    } else {
        Modifier.fillMaxWidth()
    }

    Box {
        WrappedColumn(
            modifier = scroll
                .background(MaterialTheme.colorScheme.surface)
                .align(Alignment.Center)

        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(image.fullsize)
                    .size(Size.ORIGINAL)
                    .crossfade(true)
                    .build(),
                contentDescription = image.alt,
                contentScale = ContentScale.Fit,
                modifier = scale
            )

            //if (hasAltText) {

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier
                        .padding(top = 12.dp).requiredWidthIn(
                            min = 100.dp
                        )
                        .align(Alignment.CenterHorizontally)
                ) {
                    SelectionContainer {
                        Text(
                            text = image.alt,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier.padding(top= 4.dp, bottom = 12.dp, start = 12.dp, end = 12.dp),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            //}
        }
        if(image.aspectRatio == null) {
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).heightIn(max = 1100.dp),
                adapter = rememberScrollbarAdapter(
                    scrollState = vScrollState
                ),
                style = LocalScrollbarStyle.current.copy(
                    hoverColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    unhoverColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            )

            HorizontalScrollbar(
                modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = 1000.dp),
                adapter = rememberScrollbarAdapter(
                    scrollState = hScrollState
                ),
                style = LocalScrollbarStyle.current.copy(
                    hoverColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    unhoverColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            )
        }
    }
}