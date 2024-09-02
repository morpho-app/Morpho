package com.morpho.app.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.morpho.app.model.bluesky.DraftImage

@Composable
fun ImageChooser(
    modifier: Modifier = Modifier,
) {

}

@Composable
fun ComposerThumbnail(
    image: DraftImage,
    modifier: Modifier = Modifier,
    removeCallback : () -> Unit = {},
    editAltTextCallback : (DraftImage) -> DraftImage = {it},
    editImageCallback: (DraftImage) -> DraftImage = {it},

) {
    var draftImage by remember { mutableStateOf(image) }
    var height = 120.dp
    val width = remember {
        var w = ((draftImage.aspectRatio?.height?.toFloat()?.let {
            draftImage.aspectRatio?.width?.toFloat()
                ?.div(it)
        })?.times(height.value))?.dp ?: height
        if (w < 100.dp) {
            w = 110.dp
            height = (draftImage.aspectRatio?.width?.toFloat()?.let {
                draftImage.aspectRatio?.height?.toFloat()
                    ?.div(it)
            }?.times(w.value))?.dp ?: 140.dp
            ((draftImage.aspectRatio?.height?.toFloat()?.let {
                draftImage.aspectRatio?.width?.toFloat()
                    ?.div(it)
            })?.times(height.value))?.dp ?: height
        } else w
    }
    ElevatedCard(modifier = modifier.size(width, height)) {

        val imageBitmap = draftImage.image.toImageBitmap()
        BoxWithConstraints(Modifier.fillMaxSize()) {

            if (imageBitmap != null) {

                if (draftImage.aspectRatio != null) {


                    Image(
                        bitmap = imageBitmap,
                        filterQuality = FilterQuality.High,
                        contentScale = ContentScale.Crop,
                        contentDescription = image.altText ?: "Alt text implementation needed",
                        modifier = Modifier.fillMaxSize()
                            //.size(width = width, height = height)
                            .clickable { draftImage = editAltTextCallback(draftImage) }
                    )
                } else {
                    Image(
                        bitmap = imageBitmap,
                        filterQuality = FilterQuality.High,
                        contentScale = ContentScale.Crop,
                        contentDescription = image.altText ?: "Alt text implementation needed",
                        modifier = Modifier.fillMaxSize()
                            //.size(height)
                            .clickable { draftImage = editAltTextCallback(draftImage) }
                    )
                }

            }

                if (imageBitmap != null) {
                    TextButton(
                        onClick = { draftImage = editAltTextCallback(draftImage) },
                        border = null,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.8f),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(2.dp, 0.dp),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .requiredHeightIn(25.dp, 30.dp)
                            .requiredWidthIn(50.dp, 80.dp)
                            .padding(3.dp)
                    ) {
                        Text(
                            text = "➕ ALT",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 4.dp)
                                .padding(top = 3.dp, bottom = 5.dp),

                            textAlign = TextAlign.Start
                        )
                    }
                }
            TextButton(
                onClick = { removeCallback() },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.8f),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp),

                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .requiredSizeIn(25.dp, 25.dp, 30.dp, 30.dp)
                    .padding(3.dp)

            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Image",
                    tint = Color.White,
                    modifier = Modifier
                        .requiredSizeIn(20.dp, 20.dp, 25.dp, 25.dp)
                        .padding(4.dp)
                )
            }
            if (imageBitmap != null) {
                TextButton(
                    onClick = { draftImage = editImageCallback(draftImage) },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.8f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .requiredSizeIn(25.dp, 25.dp, 30.dp, 30.dp)
                        .padding(3.dp)

                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Image",
                        tint = Color.White,
                        modifier = Modifier
                            .requiredSizeIn(20.dp, 20.dp, 25.dp, 25.dp)
                            .padding(4.dp)

                    )
                }
            }

        }
    }
}