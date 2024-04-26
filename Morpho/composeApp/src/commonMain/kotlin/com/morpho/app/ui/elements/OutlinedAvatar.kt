package com.morpho.app.ui.elements

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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.morpho.app.ui.theme.MorphoTheme
import com.morpho.app.ui.theme.roundedTopLBotR
import morpho.composeapp.generated.resources.Res
import morpho.composeapp.generated.resources.placeholder_pfp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview


enum class AvatarShape {
    Circle,
    Rounded,
    Corner
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun OutlinedAvatar(
    url: String,
    modifier: Modifier = Modifier,
    outlineSize: Dp = 0.dp,
    outlineColor: Color = MaterialTheme.colorScheme.surface,
    contentDescription: String = "",
    shape: AvatarShape = AvatarShape.Corner,
    onClicked: () -> Unit = {},
    size: Dp = 30.dp,
) {
    val s = when(shape) {
        AvatarShape.Circle -> CircleShape
        AvatarShape.Rounded -> MaterialTheme.shapes.small
        AvatarShape.Corner -> roundedTopLBotR.small
    }
    AsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        fallback = painterResource(Res.drawable.placeholder_pfp),
        placeholder = painterResource(Res.drawable.placeholder_pfp),
        modifier = modifier
            .clickable { onClicked() }
            .clip(s)
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy))
            .size(size+outlineSize)
            .border(outlineSize, outlineColor,s)
    )

}

@Preview
@Composable
private fun OutlinedAvatarPreview() {
    MorphoTheme {
        Column {
            OutlinedAvatar(url = "")
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedAvatar(url = "", shape = AvatarShape.Circle)
        }
    }
}