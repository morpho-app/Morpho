package com.morpho.app.ui.post


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.morpho.app.model.bluesky.*
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.RichTextElement
import com.morpho.app.ui.elements.WrappedColumn
import com.morpho.app.util.getFormattedDateTimeSince
import com.morpho.app.util.openBrowser
import com.morpho.app.util.parseImageFullRef
import com.morpho.app.util.parseImageThumbRef
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmbedPostFragment(
    post: EmbedPost.VisibleEmbedPost,
    modifier: Modifier = Modifier,
    onItemClicked: (AtUri) -> Unit = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
) {
    val delta = remember { getFormattedDateTimeSince(post.litePost.createdAt) }
    var hidePost by rememberSaveable { mutableStateOf(post.author.mutedByMe) }
    val muted = rememberSaveable { post.author.mutedByMe }
    WrappedColumn(
        modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        Surface (
            tonalElevation = 4.dp,
            shadowElevation = 2.dp,
            //border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.End)
                .clickable { onItemClicked(post.uri) }
        ) {
            Column(
                Modifier
                    .padding(bottom = 6.dp, end = 2.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .padding(end = 4.dp),
                    horizontalArrangement = Arrangement.End

                ) {
                    OutlinedAvatar(
                        url = post.author.avatar.orEmpty(),
                        contentDescription = "Avatar for ${post.author.handle}",
                        size = 20.dp,
                        //outlineColor = MaterialTheme.colorScheme.background,
                        onClicked = {
                            onProfileClicked(post.author.did)
                        }
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                        .times(1.2f),
                                    fontWeight = FontWeight.Medium
                                )
                            ) {
                                append(post.author.displayName.orEmpty())
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                        .times(1.0f)
                                )
                            ) {
                                append(" @${post.author.handle}")
                            }

                        },
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp, start = 4.dp)
                            .weight(10.0F)
                            .alignByBaseline()
                            .clickable { onProfileClicked(post.author.did) },
                    )
                    Spacer(
                        modifier = Modifier
                            .width(0.dp)
                            .weight(0.01F),
                    )
                    Text(
                        text = delta,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = MaterialTheme.typography.labelLarge
                            .fontSize.div(1.2F),
                        modifier = Modifier
                            .padding(top = 4.dp, end = 4.dp)
                            .wrapContentWidth(Alignment.End)
                            .weight(3.0F)
                            .alignByBaseline(),
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        softWrap = false,
                    )
                }

                RichTextElement(
                    text = post.litePost.text,
                    facets = post.litePost.facets,
                    onClick = {
                        when(it) {
                            is FacetType.ExternalLink -> {
                                openBrowser(it.uri.uri)
                            }
                            is FacetType.Format -> {}
                            is FacetType.PollBlueOption -> {

                            }
                            is FacetType.Tag -> {}
                            is FacetType.UserDidMention -> {
                                onProfileClicked(post.author.did)
                            }
                            is FacetType.UserHandleMention -> {
                                onProfileClicked(it.handle)
                            }
                            null -> {
                                onItemClicked(post.uri)
                            }

                            else -> {}
                        }
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                EmbedPostFeature(post = post, onItemClicked, onLinkClicked = {
                    openBrowser(it)
                })

            }
        }
    }

}

@Composable
fun ColumnScope.EmbedPostFeature(
    post: EmbedPost.VisibleEmbedPost,
    onItemClicked: (AtUri) -> Unit = {},
    onLinkClicked: (String) -> Unit = {},
) {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    when (post.litePost.feature) {
        is BskyPostFeature.ExternalFeature -> {
            if (post.litePost.feature.thumb?.contains("{") == true) {
                val embed = post.litePost.feature
                val thumb = remember { parseImageThumbRef(post.litePost.feature.thumb, post.author.did) }
                PostLinkEmbed(
                    linkData = BskyPostFeature.ExternalFeature(
                        uri = embed.uri,
                        title = embed.title,
                        description = embed.description,
                        thumb = thumb
                    ),
                    linkPress = {onLinkClicked(it)},
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                PostLinkEmbed(
                    linkData = post.litePost.feature,
                    linkPress = {onLinkClicked(it)},
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        is BskyPostFeature.ImagesFeature -> {
            if (post.litePost.feature.images.isNotEmpty()
                && post.litePost.feature.images.first().thumb.contains("{")
            ) {
                val images = remember {
                    post.litePost.feature.images.map {
                        EmbedImage(
                            thumb = parseImageThumbRef(it.thumb, post.author.did),
                            fullsize = parseImageFullRef(it.fullsize, post.author.did),
                            alt = it.alt
                        )
                    }}
                PostImages(imagesFeature = BskyPostFeature.ImagesFeature(images.toImmutableList()),
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                PostImages(imagesFeature = post.litePost.feature,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
        is BskyPostFeature.MediaPostFeature -> {
            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            when(post.litePost.feature.media) {
                is BskyPostFeature.ExternalFeature -> {
                    if (post.litePost.feature.media.thumb?.contains("{") == true) {
                        val embed = post.litePost.feature.media
                        val thumb = remember { parseImageThumbRef(post.litePost.feature.media.thumb, post.author.did) }
                        PostLinkEmbed(
                            linkData = BskyPostFeature.ExternalFeature(
                                uri = embed.uri,
                                title = embed.title,
                                description = embed.description,
                                thumb = thumb
                            ),
                            linkPress = {onLinkClicked(it)},
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        PostLinkEmbed(
                            linkData = post.litePost.feature.media,
                            linkPress = {onLinkClicked(it)},
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
                is BskyPostFeature.ImagesFeature -> {
                    if (post.litePost.feature.media.images.isNotEmpty()
                        && post.litePost.feature.media.images.first().thumb.contains("{")
                    ) {
                        val images = remember {
                            post.litePost.feature.media.images.map {
                                EmbedImage(
                                    thumb = parseImageThumbRef(it.thumb, post.author.did),
                                    fullsize = parseImageFullRef(it.fullsize, post.author.did),
                                    alt = it.alt
                                )
                            }}
                        PostImages(imagesFeature = BskyPostFeature.ImagesFeature(images.toImmutableList()),
                            modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        PostImages(imagesFeature = post.litePost.feature.media,
                            modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }

                else -> {}
            }
            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            when (post.litePost.feature.post) {
                is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = post.litePost.feature.post.uri)
                is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = post.litePost.feature.post.uri)
                is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(
                    post = post.litePost.feature.post,
                    onItemClicked = onItemClicked,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                else -> {}
            }
        }
        is BskyPostFeature.PostFeature -> {
            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            when (post.litePost.feature.post) {
                is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = post.litePost.feature.post.uri)
                is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = post.litePost.feature.post.uri)
                is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(
                    post = post.litePost.feature.post,
                    onItemClicked = onItemClicked,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                else -> {}
            }
        }
        null -> {}
        else -> {}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComposerPostFragment(
    post: BskyPost,
    modifier: Modifier = Modifier,
) {
    val delta = remember { getFormattedDateTimeSince(post.createdAt) }
    val lineColour = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        Surface (
            tonalElevation = 3.dp,
            shadowElevation = 1.dp,
            //border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.End)

        ) {
                Row(modifier = Modifier
                    .padding(bottom = 4.dp)
                    .padding(start = 0.dp, end = 6.dp)
                    .fillMaxWidth()

                ) {
                    OutlinedAvatar(
                        url = post.author.avatar.orEmpty(),
                        contentDescription = "Avatar for ${post.author.handle}",
                        modifier = Modifier
                            .size(40.dp),
                        outlineColor = MaterialTheme.colorScheme.background,
                    )
                    Column(
                        Modifier
                            .padding(vertical = 0.dp, horizontal = 6.dp)
                            .fillMaxWidth(),
                    ) {

                        FlowRow(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.End

                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = MaterialTheme.typography.labelLarge.fontSize
                                                .times(1.2f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) {
                                        append(post.author.displayName.orEmpty())
                                    }
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = MaterialTheme.typography.labelLarge.fontSize
                                                .times(1.0f)
                                        )
                                    ) {
                                        append(" @${post.author.handle}")
                                    }

                                },
                                maxLines = 2,
                                style = MaterialTheme.typography.labelLarge,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.Start)
                                    .weight(10.0F)
                                    .alignByBaseline()
                                ,
                            )
                        }
                        RichTextElement(text = post.text, facets = post.facets, maxLines = 5)
                    }
                }


        }
    }

}


@Composable
fun EmbedBlockedPostFragment(
    uri: AtUri
) {
    Surface (
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.inverseOnSurface),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)

    ) {
        Column {
            SelectionContainer {
                Text(
                    text = "Post by blocked or blocking user",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }

}

@Composable
fun EmbedNotFoundPostFragment(
    uri: AtUri
) {
    Surface (
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.inverseOnSurface),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)

    ) {
        Column {
            SelectionContainer {
                Text(
                    text = "Post not found",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}