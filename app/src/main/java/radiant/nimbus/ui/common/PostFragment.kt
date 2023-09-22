package radiant.nimbus.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import radiant.nimbus.model.BasicProfile
import radiant.nimbus.model.BskyLabel
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.BskyPostFeature
import radiant.nimbus.model.BskyPostReply
import radiant.nimbus.model.EmbedImage
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

fun indentLevel(level:Int) : Float {
    return (1 - (level.toFloat() / 100))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullPostFragment(
    post: BskyPost,
    modifier: Modifier = Modifier,
    onItemClicked: OnPostClicked = {},

    ) {
    val delta = remember { getFormattedDateTimeSince(post.createdAt) }
    val lineColour = MaterialTheme.colorScheme.onSurfaceVariant
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
                        append("\n@${post.author.handle}")
                    }

                },
                maxLines = 2,
                style = MaterialTheme.typography.labelLarge,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .wrapContentWidth(Alignment.Start)
                    //.weight(10.0F)
                    .alignByBaseline()
                    .padding(start = 16.dp),

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



        MarkdownText(
            markdown = post.text.replace("\n", "  \n"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp)
        )
        val timestamp = remember{post.createdAt.instant.toLocalDateTime(TimeZone.currentSystemDefault()).time}
        Text(
            text = "${LocalTime.fromSecondOfDay((timestamp.toSecondOfDay() + timestamp.toSecondOfDay()))} ∙ ${post.createdAt.instant.toLocalDateTime(TimeZone.currentSystemDefault()).date}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontSize = MaterialTheme.typography.labelLarge
                .fontSize.div(1.2F),
            modifier = Modifier
                .align(Alignment.End)
                //.weight(3.0F)
                ,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            softWrap = false,
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

) {
    val delta = remember { getFormattedDateTimeSince(post.createdAt) }
    val lineColour = MaterialTheme.colorScheme.onSurfaceVariant
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
                .fillMaxWidth(indentLevel(indentLevel))
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
                .fillMaxWidth(indentLevel(indentLevel))

            ) {


                OutlinedAvatar(
                    url = post.author.avatar.orEmpty(),
                    contentDescription = "Avatar for ${post.author.handle}",
                    modifier = Modifier
                        .size(50.dp)
                        .offset(y = 4.dp),
                    outlineColor = MaterialTheme.colorScheme.background,
                )
                Column(
                    Modifier
                        .padding(vertical = 6.dp, horizontal = 6.dp)
                        .fillMaxWidth(indentLevel(indentLevel)),
                ) {

                    FlowRow(
                        modifier = Modifier
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



                    MarkdownText(
                        markdown = post.text.replace("\n", "  \n"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 4.dp)
                    )

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
                .fillMaxWidth(indentLevel(indentLevel))

        ) {
            Column {
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
                .fillMaxWidth(indentLevel(indentLevel))

        ) {
            Column {
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

@DevicePreviews
@FontScalePreviews
@Composable
fun PreviewPostFragment() {
    NimbusTheme(darkTheme = true) {
        Column (modifier = Modifier.fillMaxWidth()
        ){
            FullPostFragment(post = testThreadRoot,
                onItemClicked = {},
                modifier = Modifier.fillMaxWidth()
            )
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