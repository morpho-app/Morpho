package radiant.nimbus.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import radiant.nimbus.model.BskyPostFeature
import radiant.nimbus.model.EmbedImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostImages(
    imagesFeature: BskyPostFeature.ImagesFeature,
) {
    val numImages = rememberSaveable { imagesFeature.images.size}
    if(numImages > 1) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(120.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(vertical = 6.dp)
                .heightIn(10.dp, 2000.dp)
        ) {
            items(imagesFeature.images) {image ->
                PostImageThumb(
                    image = image,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else if (numImages == 1) {
        PostImageThumb(image = imagesFeature.images.first(), modifier = Modifier
            .padding(vertical = 6.dp)
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
    when(displayFullView.value) {
        true -> {
            FullImageView(image = image, onDismissRequest = {displayFullView.value = false})
        }
        false -> {
            val showAltText = remember { mutableStateOf(false) }
            Box(
                modifier = modifier.padding(2.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image.thumb)
                        .crossfade(true)
                        .build(),
                    contentDescription = image.alt,
                    contentScale = ContentScale.Inside,
                    //placeholder = painterResource(R.drawable._0tigj8),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            displayFullView.value = true
                        }

                )
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
    }
    
}


@OptIn(ExperimentalLayoutApi::class)

@Composable
fun FullImageView(
    image: EmbedImage,
    modifier:Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
) {
    val hasAltText = remember { image.alt.isNotEmpty() }
    val showAltText = remember{ mutableStateOf(true)}
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
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .clickable { onDismissRequest() }) {
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
                    model = ImageRequest.Builder(LocalContext.current)
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