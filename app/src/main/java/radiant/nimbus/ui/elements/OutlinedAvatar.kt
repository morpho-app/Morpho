package radiant.nimbus.ui.elements

/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import radiant.nimbus.R
import radiant.nimbus.ui.theme.NimbusTheme


@Composable
fun OutlinedAvatar(
    url: String,
    modifier: Modifier = Modifier,
    outlineSize: Dp = 1.dp,
    outlineColor: Color = MaterialTheme.colorScheme.surface,
    contentDescription: String = "",
    circle: Boolean = false,
    onClicked: () -> Unit = {},
) {
    val shape = if(circle) {
        CircleShape
    } else {
        MaterialTheme.shapes.small.copy(topEnd = CornerSize(0.dp), bottomStart =CornerSize(0.dp))
    }
    Surface(
        shape = shape,
        color = outlineColor,
        onClick = onClicked,
        modifier = modifier
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            fallback = painterResource(R.drawable.placeholder_pfp),
            placeholder = painterResource(R.drawable.placeholder_pfp),
            modifier = Modifier
                .padding(outlineSize)
                .fillMaxSize()
                .clip(shape)
        )
    }
}

@Preview(
    name = "Outlined Avatar",
    widthDp = 40,
    heightDp = 40
)
@Composable
private fun OutlinedAvatarPreview() {
    NimbusTheme {
        Column {
            OutlinedAvatar(url = "")
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedAvatar(url = "", circle = true)
        }
    }
}