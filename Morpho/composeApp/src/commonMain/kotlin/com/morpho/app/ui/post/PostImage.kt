package com.morpho.app.ui.post


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Size
import com.morpho.app.model.bluesky.BskyPostFeature
import com.morpho.app.model.bluesky.EmbedImage
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostImages(
    imagesFeature: BskyPostFeature.ImagesFeature,
    modifier: Modifier = Modifier,
) {
    val numImages = rememberSaveable { imagesFeature.images.size}
    if(numImages > 1) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(120.dp),
            contentPadding = PaddingValues(2.dp),
            modifier = modifier
                .padding(top = 6.dp)
                .heightIn(10.dp, 700.dp)
        ) {
            items(imagesFeature.images) {image ->
                PostImageThumb(
                    image = image,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else if (numImages == 1 && imagesFeature.images.isNotEmpty()) {
        PostImageThumb(image = imagesFeature.images.first(), modifier = Modifier
            .padding(top = 6.dp)
            .heightIn(10.dp, 700.dp)
            .fillMaxWidth()
        )
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostImageThumb(
    image: EmbedImage,
    modifier: Modifier = Modifier,
) {
    val displayFullView = remember { mutableStateOf(false) }
    if(displayFullView.value){
        FullImageView(image = image, onDismissRequest = {displayFullView.value = false})
    }
    val showAltText = remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = modifier
    ) {
        if (image.aspectRatio == null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(image.thumb)
                    .build(),
                contentDescription = image.alt,
                contentScale = ContentScale.Inside,
                filterQuality = FilterQuality.High,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        displayFullView.value = true
                    }

            )
        } else {
            var width = with(LocalDensity.current) { maxWidth.value.dp.toPx() }
            var height = with(LocalDensity.current) { maxHeight.value.dp.toPx() }
            val ratio = image.aspectRatio.width.toFloat() / image.aspectRatio.height.toFloat()
            if (ratio > 1) {
                height /= ratio
                height = height.roundToInt().toFloat()
                width = width.roundToInt().toFloat()
            } else {
                width /= ratio
                width = width.roundToInt().toFloat()
                height = height.roundToInt().toFloat()
            }
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .size(Size(width.toInt(), height.toInt()))
                    .data(image.thumb)
                    .build(),
                contentDescription = image.alt,
                filterQuality = FilterQuality.High,
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        displayFullView.value = true
                    }

            )
        }

        if (image.alt.isNotEmpty()) {
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
                                text = image.alt,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(6.dp),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
                false -> {
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


@OptIn(ExperimentalLayoutApi::class)

@Composable
expect fun FullImageView(
    image: EmbedImage,
    modifier:Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
)