package radiant.nimbus.ui.post

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.collections.immutable.toImmutableList
import radiant.nimbus.api.AtUri
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.BskyPostFeature
import radiant.nimbus.model.EmbedImage
import radiant.nimbus.model.EmbedPost
import radiant.nimbus.ui.elements.OutlinedAvatar
import radiant.nimbus.util.getFormattedDateTimeSince
import radiant.nimbus.util.parseImageFullRef
import radiant.nimbus.util.parseImageThumbRef

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmbedPostFragment(
    post: EmbedPost.VisibleEmbedPost,
    modifier: Modifier = Modifier,
    role: PostFragmentRole = PostFragmentRole.Solo,
    onItemClicked: (AtUri) -> Unit = {},
    onProfileClicked: () -> Unit = {},
) {
    val delta = remember { getFormattedDateTimeSince(post.litePost.createdAt) }
    val lineColour = MaterialTheme.colorScheme.onSurfaceVariant
    val ctx = LocalContext.current
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
                .clickable{onItemClicked(post.uri)}

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
                    onClicked = onProfileClicked
                )
                Column(
                    Modifier
                        .padding(vertical = 6.dp, horizontal = 6.dp)
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
                                .clickable { onProfileClicked() }
                                ,
                        )
                        Spacer(
                            modifier = Modifier
                                .width(1.dp)
                                .weight(0.1F),
                        )
                        Text(
                            text = delta,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = MaterialTheme.typography.labelLarge
                                .fontSize.div(1.2F),
                            modifier = Modifier
                                .wrapContentWidth(Alignment.End)
                                .weight(3.0F)
                                .alignByBaseline(),
                            maxLines = 1,
                            overflow = TextOverflow.Visible,
                            softWrap = false,
                        )
                    }

                    SelectionContainer(
                        Modifier.clickable { onItemClicked(post.uri) }
                    ) {
                        MarkdownText(
                            markdown = post.litePost.text.replace("\n", "  \n"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            linkColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp).clickable { onItemClicked(post.uri) },
                            onLinkClicked = {
                                val urlIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(it)
                                )
                                ctx.startActivity(urlIntent)
                            },
                        )
                    }
                    when (post.litePost.feature) {
                        is BskyPostFeature.ExternalFeature -> {
                            if (post.litePost.feature.thumb?.contains("{") == true) {
                                val embed = post.litePost.feature
                                val thumb = remember {parseImageThumbRef(post.litePost.feature.thumb, post.author.did) }
                                PostLinkEmbed(
                                    linkData = BskyPostFeature.ExternalFeature(
                                        uri = embed.uri,
                                        title = embed.title,
                                        description = embed.description,
                                        thumb = thumb
                                    ),
                                    linkPress = {
                                        val urlIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(it)
                                        )
                                        ctx.startActivity(urlIntent)
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            } else {
                                PostLinkEmbed(
                                    linkData = post.litePost.feature,
                                    linkPress = {
                                        val urlIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(it)
                                        )
                                        ctx.startActivity(urlIntent)
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                        is BskyPostFeature.ImagesFeature -> {
                            if (post.litePost.feature.images.isNotEmpty()
                                && post.litePost.feature.images.first().thumb.contains("{")
                                ) {
                                val images = mutableListOf<EmbedImage>()
                                remember {post.litePost.feature.images.map {
                                    images.add(
                                        EmbedImage(
                                        thumb = parseImageThumbRef(it.thumb, post.author.did),
                                        fullsize = parseImageFullRef(it.fullsize, post.author.did),
                                        alt = it.alt
                                    ))
                                }}
                                PostImages(imagesFeature = BskyPostFeature.ImagesFeature(images.toImmutableList()),
                                    modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                PostImages(imagesFeature = post.litePost.feature,
                                    modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                        is BskyPostFeature.MediaPostFeature -> {
                            when(post.litePost.feature.media) {
                                is BskyPostFeature.ExternalFeature -> {
                                    if (post.litePost.feature.media.thumb?.contains("{") == true) {
                                        val embed = post.litePost.feature.media
                                        val thumb = remember {parseImageThumbRef(post.litePost.feature.media.thumb, post.author.did)}
                                        PostLinkEmbed(
                                            linkData = BskyPostFeature.ExternalFeature(
                                                uri = embed.uri,
                                                title = embed.title,
                                                description = embed.description,
                                                thumb = thumb
                                            ),
                                            linkPress = {
                                                val urlIntent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(it)
                                                )
                                                ctx.startActivity(urlIntent)
                                            },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    } else {
                                        PostLinkEmbed(
                                            linkData = post.litePost.feature.media,
                                            linkPress = {
                                                val urlIntent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(it)
                                                )
                                                ctx.startActivity(urlIntent)
                                            },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                                is BskyPostFeature.ImagesFeature -> {
                                    if (post.litePost.feature.media.images.isNotEmpty()
                                        && post.litePost.feature.media.images.first().thumb.contains("{")
                                    ) {
                                        val images = mutableListOf<EmbedImage>()
                                        remember {
                                        post.litePost.feature.media.images.map {
                                            images.add(
                                                EmbedImage(
                                                    thumb = parseImageThumbRef(it.thumb, post.author.did),
                                                    fullsize = parseImageFullRef(it.fullsize, post.author.did),
                                                    alt = it.alt
                                                ))
                                        }}
                                        PostImages(imagesFeature = BskyPostFeature.ImagesFeature(images.toImmutableList()),
                                            modifier = Modifier.align(Alignment.CenterHorizontally))
                                    } else {
                                        PostImages(imagesFeature = post.litePost.feature.media,
                                            modifier = Modifier.align(Alignment.CenterHorizontally))
                                    }
                                }
                            }
                            when (post.litePost.feature.post) {
                                is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = post.litePost.feature.post.uri)
                                is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = post.litePost.feature.post.uri)
                               is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(
                                   post = post.litePost.feature.post,
                                   onItemClicked = onItemClicked,
                                   modifier = Modifier.align(Alignment.CenterHorizontally)
                               )
                            }
                        }
                        is BskyPostFeature.PostFeature -> {
                            when (post.litePost.feature.post) {
                                is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = post.litePost.feature.post.uri)
                                is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = post.litePost.feature.post.uri)
                                is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(
                                    post = post.litePost.feature.post,
                                    onItemClicked = onItemClicked,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                        null -> {}
                    }
                }
            }
        }
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
    val ctx = LocalContext.current
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
                        .padding(vertical = 6.dp, horizontal = 6.dp)
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

                    SelectionContainer(
                    ) {
                        MarkdownText(
                            markdown = post.text.replace("\n", "  \n"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            linkColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp),
                            onLinkClicked = {
                            },
                        )
                    }

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