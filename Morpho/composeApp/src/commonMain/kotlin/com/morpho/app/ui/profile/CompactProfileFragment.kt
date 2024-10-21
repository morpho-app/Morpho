package com.morpho.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.morpho.app.model.bluesky.DetailedProfile
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.MorphoHighlightIndication
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.RichTextElement
import com.morpho.app.ui.elements.WrappedColumn
import com.morpho.app.ui.utils.ItemClicked
import com.morpho.app.ui.utils.OnItemClicked
import com.morpho.butterfly.Did

@Composable
fun CompactProfileFragment(
    profile: DetailedProfile,
    elevate: Boolean = false,
    modifier: Modifier = Modifier,
    onProfileClicked: (Did) -> Unit = {  },
    onItemClicked: OnItemClicked = ItemClicked(
        uriHandler = LocalUriHandler.current,
        navigator = LocalNavigator.currentOrThrow,
    ),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = remember { MorphoHighlightIndication() }
    WrappedColumn(modifier = modifier.fillMaxWidth()) {
        Surface (
            shadowElevation = if (elevate ) 2.dp else 0.dp,
            tonalElevation = if (elevate) 2.dp else 0.dp,
            shape = MaterialTheme.shapes.small,
            //color = bgColor,
            modifier = modifier
                .fillMaxWidth()
                .align(Alignment.End)
                .clickable(
                    interactionSource = interactionSource,
                    indication = indication,
                    enabled = true,
                    onClick = { onProfileClicked(profile.did) }
                )

        ) {
            Row(
                modifier = Modifier.padding(end = 6.dp)
                    .fillMaxWidth()//.fillMaxWidth(indentLevel(indent))
            ) {
                OutlinedAvatar(
                    url = profile.avatar.orEmpty(),
                    contentDescription = "Avatar for ${profile.displayName} ${profile.handle}",
                    size = 45.dp,
                    outlineColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(end = 2.dp),
                    avatarShape = AvatarShape.Corner
                )
                Column(
                    Modifier
                        .padding(top = 4.dp, start = 2.dp, end = 6.dp)
                        .fillMaxWidth()//.fillMaxWidth(indentLevel(indent))
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize,
                                    fontWeight = FontWeight.Medium
                                )
                            ) {
                                if (profile.displayName != null) append("${profile.displayName} ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize.times(
                                        0.8f
                                    )
                                )
                            ) {
                                append("@${profile.handle}")
                            }

                        },
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .wrapContentWidth(Alignment.Start)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = indication,
                                enabled = true,
                                onClick = { onProfileClicked(profile.did) }
                            )
                    )
                    ProfileLabels(
                        labels = profile.labels,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) { label ->

                    }
                    RichTextElement(
                        profile.description.orEmpty(),
                        maxLines = 4,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) { facetTypes ->
                        facetTypes.fastForEach {
                            onItemClicked.onRichTextFacetClicked(facet = it)
                        }
                    }

                }
            }
        }
    }
}