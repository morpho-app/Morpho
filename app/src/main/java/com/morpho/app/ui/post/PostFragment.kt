package com.morpho.app.ui.post

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.atproto.repo.StrongRef
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Cid
import com.morpho.butterfly.Did
import com.morpho.butterfly.Handle
import com.morpho.butterfly.model.RecordType
import com.morpho.app.model.BasicProfile
import com.morpho.app.model.BskyLabel
import com.morpho.app.model.BskyPost
import com.morpho.app.model.BskyPostFeature
import com.morpho.app.model.BskyPostReason
import com.morpho.app.model.BskyPostReply
import com.morpho.app.model.EmbedImage
import com.morpho.app.model.EmbedPost
import com.morpho.app.model.Moment
import com.morpho.app.model.FacetType
import com.morpho.app.ui.common.OnPostClicked
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.RichTextElement
import com.morpho.app.ui.elements.WrappedColumn
import com.morpho.app.ui.theme.MorphoTheme
import morpho.app.ui.utils.DevicePreviews
import morpho.app.ui.utils.indentLevel
import com.morpho.app.util.getFormattedDateTimeSince



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostFragment(
    post: BskyPost,
    modifier: Modifier = Modifier,
    role: PostFragmentRole = PostFragmentRole.Solo,
    indentLevel: Int = 0,
    elevate: Boolean = false,
    onItemClicked: OnPostClicked = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
) {
    val padding = remember { when(role) {
        PostFragmentRole.Solo -> Modifier.padding(2.dp)
        PostFragmentRole.PrimaryThreadRoot -> Modifier.padding(2.dp)
        PostFragmentRole.ThreadBranchStart -> Modifier.padding(start = 2.dp, top = 0.dp, end = 2.dp, bottom = 0.dp)
        PostFragmentRole.ThreadBranchMiddle -> Modifier.padding(start = 2.dp, top = 0.dp, end = 2.dp, bottom = 0.dp)
        PostFragmentRole.ThreadBranchEnd -> Modifier.padding(start = 2.dp, top = 0.dp, end = 2.dp, bottom = 0.dp)
        PostFragmentRole.ThreadRootUnfocused -> Modifier.padding(2.dp)
        PostFragmentRole.ThreadEnd -> Modifier.padding(start = 2.dp, top = 0.dp, end = 2.dp, bottom = 0.dp)
    }}
    WrappedColumn(
        modifier = padding.fillMaxWidth()
    ) {
        val delta = remember { getFormattedDateTimeSince(post.createdAt) }
        val indent = rememberSaveable { when(role) {
            PostFragmentRole.Solo -> indentLevel.toFloat()
            PostFragmentRole.PrimaryThreadRoot -> indentLevel.toFloat()
            PostFragmentRole.ThreadBranchStart -> 0.0f//indentLevel.toFloat()
            PostFragmentRole.ThreadBranchMiddle -> 0.0f//indentLevel.toFloat()-1
            PostFragmentRole.ThreadBranchEnd -> 0.0f//indentLevel.toFloat()-1
            PostFragmentRole.ThreadRootUnfocused -> indentLevel.toFloat()
            PostFragmentRole.ThreadEnd -> 0.0f
        }}
        val ctx = LocalContext.current
        var hidePost by rememberSaveable { mutableStateOf(post.author.mutedByMe) }
        val muted = rememberSaveable { post.author.mutedByMe }
        val baseShape = MaterialTheme.shapes.small
        val shape =  when(role) {
            PostFragmentRole.Solo -> baseShape
            PostFragmentRole.PrimaryThreadRoot -> baseShape
            PostFragmentRole.ThreadBranchStart -> {
                baseShape.copy(
                    bottomEnd = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp),
                )
            }
            PostFragmentRole.ThreadBranchMiddle -> {
                baseShape.copy(
                    topEnd = CornerSize(0.dp),
                    topStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp),
                )
            }
            PostFragmentRole.ThreadBranchEnd -> {
                baseShape.copy(
                    topEnd = CornerSize(0.dp),
                    topStart = CornerSize(0.dp),
                )
            }
            PostFragmentRole.ThreadRootUnfocused -> baseShape
            PostFragmentRole.ThreadEnd -> {
                baseShape.copy(
                    topEnd = CornerSize(0.dp),
                    topStart = CornerSize(0.dp),
                )
            }
        }
        val bgColor = if (role == PostFragmentRole.ThreadEnd) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme
                .surfaceColorAtElevation(if (elevate || indentLevel > 0) 2.dp else 0.dp)
        }
        Surface (
            shadowElevation = if (elevate || indentLevel > 0) 1.dp else 0.dp,
            tonalElevation = if ((elevate || indentLevel > 0) && role != PostFragmentRole.ThreadEnd) 2.dp else 0.dp,
            shape = shape,
            modifier = modifier
                .fillMaxWidth(indentLevel(indent))
                .align(Alignment.End)
                .background(bgColor, shape)
                .clickable { onItemClicked(post.uri) }

        ) {
            Row(modifier = Modifier
                .padding(bottom = 2.dp)
                .padding(start = 0.dp, end = 6.dp)
                .fillMaxWidth(indentLevel(indent))

            ) {

                if(indent < 2) {
                    OutlinedAvatar(
                        url = post.author.avatar.orEmpty(),
                        contentDescription = "Avatar for ${post.author.handle}",
                        modifier = Modifier
                            .size(45.dp),
                        outlineColor = MaterialTheme.colorScheme.background,
                        onClicked = {  onProfileClicked(post.author.did) },
                        shape = AvatarShape.Corner
                    )
                }

                Column(
                    Modifier
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                        .fillMaxWidth(indentLevel(indent)),
                ) {
                    if(post.reason is BskyPostReason.BskyPostRepost) {
                        Row(modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.height(15.dp)
                            )
                            Text(
                                text = "Reposted by ${post.reason.repostAuthor.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(start = 5.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
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
                                shape = AvatarShape.Rounded,
                                outlineColor = MaterialTheme.colorScheme.background,
                                onClicked = { onProfileClicked(post.author.did) }
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
                                    if(post.author.displayName != null) append( "${post.author.displayName} ")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = MaterialTheme.typography.labelLarge.fontSize
                                            .times(1.0f)
                                    )
                                ) {
                                    append("@${post.author.handle}")
                                }

                            },
                            maxLines = 1,
                            style = MaterialTheme.typography.labelLarge,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .wrapContentWidth(Alignment.Start)
                                .weight(10.0F)
                                .alignByBaseline()
                                .clickable { onProfileClicked(post.author.did) },

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

                    if(post.reply?.parent != null) {
                        ReplyIndicator(post.reply.parent)
                    }

                    RichTextElement(
                        text = post.text,
                        facets = post.facets,
                        onClick = {
                            when(it) {
                                is FacetType.ExternalLink -> {
                                    val urlIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(it.uri.uri)
                                    )
                                    ctx.startActivity(urlIntent)
                                }
                                is FacetType.Format -> {
                                    onItemClicked(post.uri)
                                }
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
                        }
                    )
                    PostFeatureElement(post.feature, onItemClicked)

                    PostActions(post = post,
                        onLikeClicked = {
                            onLikeClicked(StrongRef(post.uri, post.cid))
                        },
                        onMenuClicked = onMenuClicked,
                        onReplyClicked = {
                            onReplyClicked(post)
                        },
                        onRepostClicked = {
                            onRepostClicked(post)
                        },
                        onUnClicked = onUnClicked,
                    )
                }
            }


        }
    }

}

@Composable
inline fun ReplyIndicator(
    parent: BskyPost,
    onClick: (AtIdentifier) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .offset(x = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Reply,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.height(15.dp)
        )
        Text(
            text = "Reply to ${parent.author.displayName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}

@Composable
inline fun ColumnScope.PostFeatureElement(
    feature: BskyPostFeature? = null,
    crossinline onItemClicked: OnPostClicked = {},
) {
    val ctx = LocalContext.current
    when (feature) {
        is BskyPostFeature.ExternalFeature -> PostLinkEmbed(linkData = feature,
            linkPress = {
                val urlIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(it)
                )
                ctx.startActivity(urlIntent)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally))
        is BskyPostFeature.ImagesFeature -> {
            PostImages(imagesFeature = feature,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        is BskyPostFeature.MediaPostFeature -> {
            when(feature.media) {
                is BskyPostFeature.ExternalFeature -> {
                    PostLinkEmbed(
                        linkData = feature.media,
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
                is BskyPostFeature.ImagesFeature -> {
                    PostImages(imagesFeature = feature.media,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                else -> {}
            }
            when (feature.post) {
                is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = feature.post.uri)
                is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = feature.post.uri)
                is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(
                    post = feature.post,
                    onItemClicked =  {onItemClicked(feature.post.uri)},
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                else -> {}
            }
        }
        is BskyPostFeature.PostFeature -> {
            when (feature.post) {
                is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = feature.post.uri)
                is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = feature.post.uri)
                is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(
                    post = feature.post,
                    onItemClicked =  {onItemClicked(feature.post.uri)},
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                else -> {}
            }
        }
        null -> {}
        else -> {}
    }
}


@DevicePreviews
//@FontScalePreviews
@Composable
fun PreviewPostFragments() {
    MorphoTheme(darkTheme = true) {
        Column (modifier = Modifier.fillMaxWidth()
        ){
            PostFragment(
                post = testPost,
                onItemClicked = {},
                modifier = Modifier.fillMaxWidth(),
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
    tags = persistentListOf(),
    text = "This small terrorist (pictured, indignant about not being fed in the last hour) managed to knock over a chair in the night.",
    facets = persistentListOf(),
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
    facets = persistentListOf(),
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
    tags = persistentListOf(),
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
    facets = persistentListOf(),
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
    tags = persistentListOf(),
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
    facets = persistentListOf(),
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
    tags = persistentListOf(),
    reply = BskyPostReply(root = testThreadRoot, parent = testReply1),
    reason = null
)