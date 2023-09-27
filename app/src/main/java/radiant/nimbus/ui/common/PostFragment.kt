package radiant.nimbus.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import radiant.nimbus.model.BasicProfile
import radiant.nimbus.model.BskyLabel
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.BskyPostFeature
import radiant.nimbus.model.BskyPostReply
import radiant.nimbus.model.EmbedImage
import radiant.nimbus.model.EmbedPost
import radiant.nimbus.model.Moment
import radiant.nimbus.ui.elements.dpToPx
import radiant.nimbus.ui.theme.NimbusTheme
import radiant.nimbus.ui.utils.DevicePreviews
import radiant.nimbus.ui.utils.FontScalePreviews
import radiant.nimbus.util.getFormattedDateTimeSince
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Cid
import sh.christian.ozone.api.Did
import sh.christian.ozone.api.Handle

enum class PostFragmentRole {
    Solo,
    PrimaryThreadRoot,
    ThreadBranchStart,
    ThreadBranchMiddle,
    ThreadBranchEnd,
    ThreadRootUnfocused,
}

fun indentLevel(level:Float) : Float {
    return (1 - (level / 100))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullPostFragment(
    post: BskyPost,
    modifier: Modifier = Modifier,
    onItemClicked: OnPostClicked = {},
    onProfileClicked: () -> Unit = {},
    ) {
    val delta = rememberSaveable { getFormattedDateTimeSince(post.createdAt) }
    val timestamp = remember { post.createdAt.instant.toLocalDateTime(TimeZone.currentSystemDefault()).time }
    val postDate = remember { post.createdAt.instant.toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val lineColour = MaterialTheme.colorScheme.onSurfaceVariant
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val diff = remember { today.toEpochDays() - postDate.toEpochDays() }
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 4.dp)
            .padding(start = 6.dp, end = 6.dp)
    ) {

        FlowRow(
            modifier = Modifier
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.End

        ) {
            OutlinedAvatar(
                url = post.author.avatar.orEmpty(),
                contentDescription = "Avatar for ${post.author.handle}",
                modifier = Modifier
                    .size(50.dp),
                outlineColor = MaterialTheme.colorScheme.background,
                onClicked = onProfileClicked
            )
            SelectionContainer {
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
                        if (diff < 179) {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.labelMedium.fontSize
                                        .times(1.0f),
                                    baselineShift = BaselineShift(.1f)
                                )
                            ) {
                                append("  •  $delta")
                            }
                        }
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = MaterialTheme.typography.labelLarge.fontSize
                                    .times(1.0f)
                            )
                        ) {
                            append("\n@${post.author.handle}")
                        }

                    },
                    maxLines = 2,
                    style = MaterialTheme.typography.labelLarge,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .wrapContentWidth(Alignment.Start)
                        .padding(bottom = 12.dp)
                        .alignByBaseline()
                        .padding(start = 16.dp),

                    )
            }
            Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(0.1F),
            )
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface)
            }

        }


        SelectionContainer {
            MarkdownText(
                markdown = post.text.replace("\n", "  \n"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
            )
        }
        val postTimestamp = remember {
            val seconds = post.createdAt.instant.epochSeconds % 60
            Instant.fromEpochSeconds(post.createdAt.instant.epochSeconds - seconds).toLocalDateTime(TimeZone.currentSystemDefault()).time
        }
        when (post.feature) {
            is BskyPostFeature.ExternalFeature -> PostLinkEmbed(linkData = post.feature)
            is BskyPostFeature.ImagesFeature -> PostImages(imagesFeature = post.feature)
            is BskyPostFeature.MediaPostFeature -> {
                when(post.feature.media) {
                    is BskyPostFeature.ExternalFeature -> PostLinkEmbed(linkData = post.feature.media)
                    is BskyPostFeature.ImagesFeature -> PostImages(imagesFeature = post.feature.media)
                }
                when (post.feature.post) {
                    is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = post.feature.post.uri)
                    is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = post.feature.post.uri)
                    is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(post = post.feature.post)
                }
            }
            is BskyPostFeature.PostFeature -> {
                when (post.feature.post) {
                    is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = post.feature.post.uri)
                    is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = post.feature.post.uri)
                    is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(post = post.feature.post)
                }
            }
            null -> {}
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            PostActions(
                post = post,
                showMenu = false
            )
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .weight(0.1F),
            )
            SelectionContainer {
                Text(
                    text = "$postDate at $postTimestamp",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = MaterialTheme.typography.labelLarge
                        .fontSize.div(1.2F),
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                )
            }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostActions(
    post: BskyPost,
    modifier: Modifier = Modifier,
    showMenu: Boolean = true,
    onReplyClicked: () -> Unit = {},
    onRepostClicked: () -> Unit = {},
    onLikeClicked: () -> Unit = {},
    onMenuClicked: () -> Unit = {},
) {
    FlowRow {
        PostAction(
            parameter = post.replyCount,
            iconNormal = Icons.Outlined.ChatBubbleOutline,
            iconActive = Icons.Default.ChatBubble,
            contentDescription = "Reply"
        )
        PostAction(parameter = post.repostCount,
            iconNormal = Icons.Outlined.Repeat,
            contentDescription = "Repost")
        PostAction(parameter = post.likeCount,
            iconNormal = Icons.Outlined.FavoriteBorder,
            iconActive = Icons.Default.Favorite,
            contentDescription = "Like")
        if (showMenu) {
            PostAction(parameter = -1,
                iconNormal = Icons.Default.MoreHoriz,
                contentDescription = "More"
            )
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostAction(
    parameter: Long,
    iconNormal: ImageVector,
    modifier: Modifier = Modifier,
    iconActive: ImageVector = iconNormal,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String,
    onClicked: () -> Unit = {},
    onUnClicked: () -> Unit = {},
) {
    val clicked = rememberSaveable { mutableStateOf(false) }
    val inactiveColor = MaterialTheme.colorScheme.onSurface
    val color = remember { mutableStateOf(if (clicked.value) activeColor else inactiveColor) }
    val icon = remember { mutableStateOf(if (clicked.value) iconActive else iconNormal) }
    TextButton(
        onClick = {
            if (!clicked.value) {
                clicked.value = true
                color.value = activeColor
                icon.value = iconActive
                onClicked.invoke()
            } else {
                clicked.value = false
                color.value = inactiveColor
                icon.value = iconNormal
                onUnClicked.invoke()
            }
        },
        modifier = Modifier
            .padding(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(imageVector = icon.value,
            contentDescription = contentDescription,
            tint = color.value,
            modifier = Modifier
                .size(20.dp)
                .padding(0.dp)
        )
        val text = if (parameter >= 0) parameter.toString() else ""
        Text(
            text = text,
            color = color.value,
            modifier = Modifier.padding(horizontal = 6.dp)//.offset(y=(-1).dp)
            )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostFragment(
    post: BskyPost,
    modifier: Modifier = Modifier,
    role: PostFragmentRole = PostFragmentRole.Solo,
    indentLevel: Int = 0,
    onItemClicked: OnPostClicked = {},
    onProfileClicked: () -> Unit = {},
) {
    val delta = remember { getFormattedDateTimeSince(post.createdAt) }
    val lineColour = MaterialTheme.colorScheme.onSurfaceVariant
    val padding = remember { when(role) {
        PostFragmentRole.Solo -> Modifier.padding(2.dp)
        PostFragmentRole.PrimaryThreadRoot -> Modifier.padding(2.dp)
        PostFragmentRole.ThreadBranchStart -> Modifier.padding(start = 2.dp, top = 2.dp, end = 2.dp, bottom = 0.dp)
        PostFragmentRole.ThreadBranchMiddle -> Modifier.padding(start = 2.dp, top = 0.dp, end = 2.dp, bottom = 0.dp)
        PostFragmentRole.ThreadBranchEnd -> Modifier.padding(start = 2.dp, top = 0.dp, end = 2.dp, bottom = 2.dp)
        PostFragmentRole.ThreadRootUnfocused -> Modifier.padding(2.dp)
    }}
    val indent = remember { when(role) {
        PostFragmentRole.Solo -> indentLevel.toFloat()
        PostFragmentRole.PrimaryThreadRoot -> indentLevel.toFloat()
        PostFragmentRole.ThreadBranchStart -> 0.0f//indentLevel.toFloat()
        PostFragmentRole.ThreadBranchMiddle -> 0.0f//indentLevel.toFloat()-1
        PostFragmentRole.ThreadBranchEnd -> 0.0f//indentLevel.toFloat()-1
        PostFragmentRole.ThreadRootUnfocused -> indentLevel.toFloat()
    }}
    Column(
        modifier = Modifier.padding(4.dp)
            .fillMaxWidth()
    ) {
        val shape = when(role) {
            PostFragmentRole.Solo -> MaterialTheme.shapes.small
            PostFragmentRole.PrimaryThreadRoot -> MaterialTheme.shapes.small
            PostFragmentRole.ThreadBranchStart -> {
                MaterialTheme.shapes.small.copy(
                    bottomEnd = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp),
                )
            }
            PostFragmentRole.ThreadBranchMiddle -> {
                MaterialTheme.shapes.small.copy(
                    topEnd = CornerSize(0.dp),
                    topStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp),
                )
            }
            PostFragmentRole.ThreadBranchEnd -> {
                MaterialTheme.shapes.small.copy(
                    topEnd = CornerSize(0.dp),
                    topStart = CornerSize(0.dp),
                )
            }
            PostFragmentRole.ThreadRootUnfocused -> MaterialTheme.shapes.small
        }
        Surface (
            shadowElevation = if (indentLevel > 0) 1.dp else 0.dp,
            tonalElevation = if (indentLevel > 0) 2.dp else 0.dp,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth(indentLevel(indent))
                .align(Alignment.End)

        ) {
            Row(modifier = Modifier
                .padding(vertical = 4.dp)
                .padding(start = 6.dp, end = 6.dp)
                .drawWithCache {
                    val path = Path()
                    if (false) {
                        path.moveTo(dpToPx(8.dp), dpToPx(29.dp))
                        path.lineTo(-dpToPx(8.dp), dpToPx(29.dp))
                        path.close()
                    }
                    onDrawBehind {
                        drawPath(path, lineColour, style = Stroke(width = 5f))
                    }
                }
                .fillMaxWidth(indentLevel(indent))

            ) {

                if(indent < 2) {
                    OutlinedAvatar(
                        url = post.author.avatar.orEmpty(),
                        contentDescription = "Avatar for ${post.author.handle}",
                        modifier = Modifier
                            .size(40.dp)
                            .offset(y = 4.dp),
                        outlineColor = MaterialTheme.colorScheme.background,
                        onClicked = onProfileClicked
                    )
                }

                Column(
                    Modifier
                        .padding(vertical = 6.dp, horizontal = 6.dp)
                        .fillMaxWidth(indentLevel(indent)),
                ) {
                    SelectionContainer {
                        FlowRow(
                            modifier = Modifier
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.End

                        ) {
                            if(indent >= 2) {
                                OutlinedAvatar(
                                    url = post.author.avatar.orEmpty(),
                                    contentDescription = "Avatar for ${post.author.handle}",
                                    modifier = Modifier
                                        //.offset(y = 4.dp)
                                        .size(30.dp),

                                    outlineColor = MaterialTheme.colorScheme.background,
                                    onClicked = onProfileClicked
                                )
                            }
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
                                    //.weight(10.0F)
                                    .alignByBaseline(),

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
                                    //.weight(3.0F)
                                    .alignByBaseline(),
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                softWrap = false,
                            )
                        }
                    }


                    SelectionContainer {
                        MarkdownText(
                            markdown = post.text.replace("\n", "  \n"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }
                    when (post.feature) {
                        is BskyPostFeature.ExternalFeature -> PostLinkEmbed(linkData = post.feature)
                        is BskyPostFeature.ImagesFeature -> PostImages(imagesFeature = post.feature)
                        is BskyPostFeature.MediaPostFeature -> {
                            when(post.feature.media) {
                                is BskyPostFeature.ExternalFeature -> PostLinkEmbed(linkData = post.feature.media)
                                is BskyPostFeature.ImagesFeature -> PostImages(imagesFeature = post.feature.media)
                            }
                            when (post.feature.post) {
                                is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = post.feature.post.uri)
                                is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = post.feature.post.uri)
                                is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(post = post.feature.post)
                            }
                        }
                        is BskyPostFeature.PostFeature -> {
                            when (post.feature.post) {
                                is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = post.feature.post.uri)
                                is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = post.feature.post.uri)
                                is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(post = post.feature.post)
                            }
                        }
                        null -> {}
                    }

                    PostActions(post = post)
                }
            }
        }
    }

}


@Composable
fun BlockedPostFragment(
    modifier: Modifier = Modifier,
    indentLevel: Int = 0,
    role: PostFragmentRole = PostFragmentRole.Solo,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        Surface (
            shadowElevation = max((indentLevel-1).dp, 0.dp),
            tonalElevation = indentLevel.dp,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth(indentLevel(indentLevel.toFloat()))

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
}

@Composable
fun NotFoundPostFragment(
    modifier: Modifier = Modifier,
     indentLevel: Int = 0,
     role: PostFragmentRole = PostFragmentRole.Solo,
 ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        Surface (
            shadowElevation = max((indentLevel-1).dp, 0.dp),
            tonalElevation = indentLevel.dp,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth(indentLevel(indentLevel.toFloat()))

        ) {
            Column {
                SelectionContainer {
                    Text(
                        text = "Post deleted or not found",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@DevicePreviews
@FontScalePreviews
@Composable
fun PreviewFullPostFragment() {
    NimbusTheme(darkTheme = false) {
        Column (modifier = Modifier.fillMaxWidth()
        ){
            FullPostFragment(post = testThreadRoot,
                onItemClicked = {},
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}


@DevicePreviews
//@FontScalePreviews
@Composable
fun PreviewPostFragments() {
    NimbusTheme(darkTheme = true) {
        Column (modifier = Modifier.fillMaxWidth()
        ){
            PostFragment(
                post = testPost,
                onItemClicked = {},
                modifier = Modifier.fillMaxWidth()
            )
            BlockedPostFragment()
            NotFoundPostFragment()
        }

    }
}


val testPost = BskyPost(
    uri = AtUri( "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.feed.post/3jt3tt6wrfm2a"),
    cid = Cid("bafyreigndfgfzibrqazcddj3lx3ivvnfzq3keqzfrtw5dltkc6l6l4wgsi"),
    author = BasicProfile(
        did = Did("did:plc:yfvwmnlztr4dwkb7hwz55r2g"),
        handle = Handle("nonbinary.computer"),
        displayName = "Orual",
        avatar = "https://av-cdn.bsky.app/img/avatar/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreifpzcenp6rhmxohv3kkez4uv4ldjphiysmju6scwgne34nb245wra@jpeg",
        mutedByMe = false,
        followingMe = false,
        followedByMe = false,
        labels = persistentListOf(
            BskyLabel(value = "testLabel")
        )

    ),
    text = "This small terrorist (pictured, indignant about not being fed in the last hour) managed to knock over a chair in the night.",
    textLinks = persistentListOf(),
    createdAt = Moment(instant = Instant.parse("2023-04-11T12:15:26.077Z")),
    feature = BskyPostFeature.ImagesFeature(
        images = persistentListOf(
            EmbedImage(
                thumb = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                fullsize = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                alt = "alt text"
            ),
            EmbedImage(
                thumb = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                fullsize = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                alt = "alt text"
            ),
            EmbedImage(
                thumb = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                fullsize = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                alt = "alt text"
            ),
            EmbedImage(
                thumb = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                fullsize = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                alt = "alt text"
            ),
        )

    ),
    replyCount = 0,
    repostCount = 2,
    likeCount = 5,
    indexedAt = Moment(instant = Instant.parse("2023-04-11T12:36:26.077Z")),
    reposted = false,
    liked = false,
    labels = persistentListOf(
        BskyLabel(value = "testLabel")
    ),
    reply = null,
    reason = null
)




val testThreadRoot = BskyPost(
    uri = AtUri( "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.feed.post/3jt3tt6wrfm2a"),
    cid = Cid("bafyreigndfgfzibrqazcddj3lx3ivvnfzq3keqzfrtw5dltkc6l6l4wgsi"),
    author = BasicProfile(
        did = Did("did:plc:yfvwmnlztr4dwkb7hwz55r2g"),
        handle = Handle("nonbinary.computer"),
        displayName = "Orual",
        avatar = "https://av-cdn.bsky.app/img/avatar/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreifpzcenp6rhmxohv3kkez4uv4ldjphiysmju6scwgne34nb245wra@jpeg",
        mutedByMe = false,
        followingMe = false,
        followedByMe = false,
        labels = persistentListOf(
            BskyLabel(value = "testLabel")
        )

    ),
    text = "This small terrorist (pictured, indignant about not being fed in the last hour) managed to knock over a chair in the night.",
    textLinks = persistentListOf(),
    createdAt = Moment(instant = Instant.parse("2023-09-23T12:15:26.077Z")),
    feature = testLinkEmbed,
    replyCount = 0,
    repostCount = 2,
    likeCount = 5,
    indexedAt = Moment(instant = Instant.parse("2023-04-11T12:36:26.077Z")),
    reposted = false,
    liked = false,
    labels = persistentListOf(
        BskyLabel(value = "testLabel")
    ),
    reply = null,
    reason = null
)

val testReply1 = BskyPost(
    uri = AtUri( "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.feed.post/3jt3tt6wrfm2a"),
    cid = Cid("bafyreigndfgfzibrqazcddj3lx3ivvnfzq3keqzfrtw5dltkc6l6l4wgsi"),
    author = BasicProfile(
        did = Did("did:plc:yfvwmnlztr4dwkb7hwz55r2g"),
        handle = Handle("nonbinary.computer"),
        displayName = "Orual",
        avatar = "https://av-cdn.bsky.app/img/avatar/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreifpzcenp6rhmxohv3kkez4uv4ldjphiysmju6scwgne34nb245wra@jpeg",
        mutedByMe = false,
        followingMe = false,
        followedByMe = false,
        labels = persistentListOf(
            BskyLabel(value = "testLabel")
        )

    ),
    text = "This small terrorist (pictured, indignant about not being fed in the last hour) managed to knock over a chair in the night.",
    textLinks = persistentListOf(),
    createdAt = Moment(instant = Instant.parse("2023-04-11T12:15:26.077Z")),
    feature = BskyPostFeature.ImagesFeature(
        images = persistentListOf(
            EmbedImage(
                thumb = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                fullsize = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                alt = "alt text"
            ),
            EmbedImage(
                thumb = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                fullsize = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                alt = "alt text"
            ),
        )

    ),
    replyCount = 0,
    repostCount = 2,
    likeCount = 5,
    indexedAt = Moment(instant = Instant.parse("2023-04-11T12:36:26.077Z")),
    reposted = false,
    liked = false,
    labels = persistentListOf(
        BskyLabel(value = "testLabel")
    ),
    reply = BskyPostReply(root = testThreadRoot, parent = testThreadRoot),
    reason = null
)

val testReply2 = BskyPost(
    uri = AtUri( "at://did:plc:yfvwmnlztr4dwkb7hwz55r2g/app.bsky.feed.post/3jt3tt6wrfm2a"),
    cid = Cid("bafyreigndfgfzibrqazcddj3lx3ivvnfzq3keqzfrtw5dltkc6l6l4wgsi"),
    author = BasicProfile(
        did = Did("did:plc:yfvwmnlztr4dwkb7hwz55r2g"),
        handle = Handle("nonbinary.computer"),
        displayName = "Orual",
        avatar = "https://av-cdn.bsky.app/img/avatar/plain/did:plc:yfvwmnlztr4dwkb7hwz55r2g/bafkreifpzcenp6rhmxohv3kkez4uv4ldjphiysmju6scwgne34nb245wra@jpeg",
        mutedByMe = false,
        followingMe = false,
        followedByMe = false,
        labels = persistentListOf(
            BskyLabel(value = "testLabel")
        )

    ),
    text = "This small terrorist (pictured, indignant about not being fed in the last hour) managed to knock over a chair in the night.",
    textLinks = persistentListOf(),
    createdAt = Moment(instant = Instant.parse("2023-04-11T12:15:26.077Z")),
    feature = BskyPostFeature.ImagesFeature(
        images = persistentListOf(
            EmbedImage(
                thumb = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                fullsize = "bafkreig3peejkdukuqnc3plrbqoz7kemh74c345hxpry7uxuftfp57wyai",
                alt = "alt text"
            )
        )

    ),
    replyCount = 0,
    repostCount = 2,
    likeCount = 5,
    indexedAt = Moment(instant = Instant.parse("2023-04-11T12:36:26.077Z")),
    reposted = false,
    liked = false,
    labels = persistentListOf(
        BskyLabel(value = "testLabel")
    ),
    reply = BskyPostReply(root = testThreadRoot, parent = testReply1),
    reason = null
)