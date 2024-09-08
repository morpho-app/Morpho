package com.morpho.app.ui.post


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.util.fastForEach
import com.morpho.app.model.bluesky.*
import com.morpho.app.ui.elements.MorphoHighlightIndication
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.RichTextElement
import com.morpho.app.ui.elements.WrappedColumn
import com.morpho.app.ui.lists.FeedListEntryFragment
import com.morpho.app.ui.lists.UserListEntryFragment
import com.morpho.app.util.getFormattedDateTimeSince
import com.morpho.app.util.openBrowser
import com.morpho.app.util.parseImageFullRef
import com.morpho.app.util.parseImageThumbRef
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmbedPostFragment(
    post: EmbedRecord.VisibleEmbedPost,
    modifier: Modifier = Modifier,
    onItemClicked: (AtUri) -> Unit = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
) {
    val delta = remember { getFormattedDateTimeSince(post.litePost.createdAt) }
    var hidePost by rememberSaveable { mutableStateOf(post.author.mutedByMe) }
    val muted = rememberSaveable { post.author.mutedByMe }
    val interactionSource = remember { MutableInteractionSource() }
    val indication = remember { MorphoHighlightIndication() }
    WrappedColumn(
        modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    ) {
        Surface (
            tonalElevation = 4.dp,
            shadowElevation = 2.dp,
            //border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.End)
                .clickable(
                    interactionSource = interactionSource,
                    indication = indication,
                    enabled = true,
                    onClick = { onItemClicked(post.uri) }
                )

        ) {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .padding(end = 6.dp),
                    horizontalArrangement = Arrangement.End

                ) {
                    OutlinedAvatar(
                        url = post.author.avatar.orEmpty(),
                        contentDescription = "Avatar for ${post.author.handle}",
                        size = 25.dp,
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
                            .clickable(
                                interactionSource = interactionSource,
                                indication = indication,
                                enabled = true,
                                onClick = { onProfileClicked(post.author.did) }
                            ),
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
                    onClick = { facetTypes ->
                        if (facetTypes.isEmpty()) {
                            onItemClicked(post.uri)
                            return@RichTextElement
                        }
                        facetTypes.fastForEach {
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

                                else -> {}
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                EmbedPostFeature(embed = post, onItemClicked, onLinkClicked = {
                    openBrowser(it)
                })

            }


        }
    }

}

@Composable
fun ColumnScope.EmbedPostFeature(
    embed: EmbedRecord,
    onItemClicked: (AtUri) -> Unit = {},
    onLinkClicked: (String) -> Unit = {},
) {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    when(embed) {
        is EmbedRecord.BlockedEmbedPost -> {
            EmbedBlockedPostFragment(uri = embed.uri)
        }
        is EmbedRecord.DetachedQuotePost -> {
            DetachedQuotePostFragment(uri = embed.uri)
        }
        is EmbedRecord.EmbedFeed -> {
            FeedListEntryFragment(
                embed.feed,
                onFeedClicked = {  }
            )
        }
        is EmbedRecord.EmbedLabelService -> {
            Text(text = "Label Service")
        }
        is EmbedRecord.EmbedList -> {
            UserListEntryFragment(
                list = embed.list,
                onListClicked = {

                }
            )
        }
        is EmbedRecord.InvisibleEmbedPost -> {
            EmbedNotFoundPostFragment(uri = embed.uri)
        }
        is EmbedRecord.VisibleEmbedPost -> {
            when (embed.litePost.feature) {
                is BskyPostFeature.ExternalFeature -> {
                    if (embed.litePost.feature.thumb?.contains("{") == true) {
                        val linkEmbed = embed.litePost.feature
                        val thumb = remember { parseImageThumbRef(embed.litePost.feature.thumb, embed.author.did) }
                        PostLinkEmbed(
                            linkData = BskyPostFeature.ExternalFeature(
                                uri = linkEmbed.uri,
                                title = linkEmbed.title,
                                description = linkEmbed.description,
                                thumb = thumb
                            ),
                            linkPress = {onLinkClicked(it)},
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        PostLinkEmbed(
                            linkData = embed.litePost.feature,
                            linkPress = {onLinkClicked(it)},
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
                is BskyPostFeature.ImagesFeature -> {
                    if (embed.litePost.feature.images.isNotEmpty()
                        && !embed.litePost.feature.images.first().thumb.startsWith("http")
                    ) {
                        val images = remember {
                            embed.litePost.feature.images.map {
                                EmbedImage(
                                    thumb = parseImageThumbRef(it.thumb, embed.author.did),
                                    fullsize = parseImageFullRef(it.fullsize, embed.author.did),
                                    alt = it.alt
                                )
                            }}
                        PostImages(imagesFeature = BskyPostFeature.ImagesFeature(images),
                                   modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        PostImages(imagesFeature = embed.litePost.feature,
                                   modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
                is BskyPostFeature.MediaRecordFeature -> {
                    when(embed.litePost.feature.media) {
                        is BskyPostFeature.ExternalFeature -> {
                            if (embed.litePost.feature.media.thumb?.contains("{") == true) {
                                val linkEmbed = embed.litePost.feature.media
                                val thumb = remember { parseImageThumbRef(embed.litePost.feature.media.thumb, embed.author.did) }
                                PostLinkEmbed(
                                    linkData = BskyPostFeature.ExternalFeature(
                                        uri = linkEmbed.uri,
                                        title = linkEmbed.title,
                                        description = linkEmbed.description,
                                        thumb = thumb
                                    ),
                                    linkPress = {onLinkClicked(it)},
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            } else {
                                PostLinkEmbed(
                                    linkData = embed.litePost.feature.media,
                                    linkPress = {onLinkClicked(it)},
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                        is BskyPostFeature.ImagesFeature -> {
                            if (embed.litePost.feature.media.images.isNotEmpty()
                                && embed.litePost.feature.media.images.first().thumb.contains("{")
                            ) {
                                val images = remember {
                                    embed.litePost.feature.media.images.map {
                                        EmbedImage(
                                            thumb = parseImageThumbRef(it.thumb, embed.author.did),
                                            fullsize = parseImageFullRef(it.fullsize, embed.author.did),
                                            alt = it.alt
                                        )
                                    }}
                                PostImages(imagesFeature = BskyPostFeature.ImagesFeature(images),
                                           modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                PostImages(imagesFeature = embed.litePost.feature.media,
                                           modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }

                        else -> {}
                    }
                    when (embed.litePost.feature.record) {
                        is EmbedRecord.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = embed.litePost.feature.record.uri)
                        is EmbedRecord.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = embed.litePost.feature.record.uri)
                        is EmbedRecord.VisibleEmbedPost -> EmbedPostFragment(
                            post = embed.litePost.feature.record,
                            onItemClicked = onItemClicked,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        is EmbedRecord.EmbedList -> {}
                        is EmbedRecord.EmbedFeed -> {
                            FeedListEntryFragment(
                                embed.litePost.feature.record.feed,
                                onFeedClicked = {  }
                            )
                        }
                        is EmbedRecord.EmbedLabelService -> {}
                        else -> {}
                    }
                }
                is BskyPostFeature.RecordFeature -> {
                    when (embed.litePost.feature.record) {
                        is EmbedRecord.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = embed.litePost.feature.record.uri)
                        is EmbedRecord.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = embed.litePost.feature.record.uri)
                        is EmbedRecord.VisibleEmbedPost -> EmbedPostFragment(
                            post = embed.litePost.feature.record,
                            onItemClicked = onItemClicked,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        is EmbedRecord.EmbedList -> {
                            UserListEntryFragment(
                                list = embed.litePost.feature.record.list,
                                onListClicked = {  }
                            )
                        }
                        is EmbedRecord.EmbedFeed -> {
                            FeedListEntryFragment(
                                embed.litePost.feature.record.feed,
                                onFeedClicked = {  }
                            )
                        }
                        is EmbedRecord.EmbedLabelService -> {}
                        else -> {}
                    }
                }
                null -> {}
                else -> {}
            }
        }

        is EmbedRecord.EmbedVideo -> {
            VideoEmbedThumb(
                video = embed.video,
                alt = embed.alt,
                aspectRatio = embed.aspectRatio,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .heightIn(100.dp, 600.dp)
                    .fillMaxWidth(),
            )
        }
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
fun DetachedQuotePostFragment(
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
                    text = "User has detached their post from this quote",
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