package radiant.nimbus.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import radiant.nimbus.R
import radiant.nimbus.model.BskyPostFeature
import radiant.nimbus.model.EmbedImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostImages(imagesFeature: BskyPostFeature.ImagesFeature) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(140.dp),
        contentPadding = PaddingValues(2.dp),
        modifier = Modifier.padding(vertical = 6.dp)) {
        items(imagesFeature.images) {image ->
            PostImageThumb(image = image)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostImageThumb(
    image: EmbedImage,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier.padding(2.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.thumb)
                .crossfade(true)
                .build(),
            contentDescription = image.alt,
            contentScale = ContentScale.Fit,
            placeholder = painterResource(R.drawable._0tigj8),

        )
        if (image.alt.isNotEmpty()) {
            TextButton(
                onClick = {},
                border = null,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.LightGray
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
                    modifier = Modifier.padding(0.dp),
                    textAlign = TextAlign.Start
                )
            }
        }

    }
    
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullImageView(
    image: EmbedImage,
    modifier:Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(onClick = onClose) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Return to Post")
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.fullsize)
                .crossfade(true)
                .build(),
            contentDescription = image.alt,
            contentScale = ContentScale.Fit,
            placeholder = painterResource(R.drawable._0tigj8),
            modifier = Modifier.fillMaxSize()
        )
        SelectionContainer { Text(image.alt) }

    }
}