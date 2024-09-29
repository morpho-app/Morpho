package com.morpho.app.ui.post


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.util.fastAny
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.atproto.label.Blurs
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.BskyPostFeature
import com.morpho.app.model.bluesky.BskyPostReason
import com.morpho.app.model.bluesky.EmbedRecord
import com.morpho.app.model.bluesky.FacetType
import com.morpho.app.model.bluesky.TimelinePostMedia
import com.morpho.app.ui.common.OnPostClicked
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.ContentHider
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.elements.MorphoHighlightIndication
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.RichTextElement
import com.morpho.app.ui.elements.WrappedColumn
import com.morpho.app.ui.lists.FeedListEntryFragment
import com.morpho.app.ui.lists.UserListEntryFragment
import com.morpho.app.ui.utils.ItemClicked
import com.morpho.app.ui.utils.OnFacetClicked
import com.morpho.app.ui.utils.OnItemClicked
import com.morpho.app.util.getFormattedDateTimeSince
import com.morpho.app.util.openBrowser
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.ContentHandling
import com.morpho.butterfly.LabelAction
import com.morpho.butterfly.LabelDescription
import com.morpho.butterfly.LabelIcon
import com.morpho.butterfly.model.RecordType
import kotlinx.collections.immutable.toImmutableList
import morpho.app.ui.utils.indentLevel
import morpho.composeapp.generated.resources.Res
import morpho.composeapp.generated.resources.replyIndicator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun PostFragment(
    post: BskyPost,
    modifier: Modifier = Modifier,
    role: PostFragmentRole = PostFragmentRole.Solo,
    indentLevel: Int = 0,
    elevate: Boolean = false,
    onItemClicked: OnItemClicked = ItemClicked(
        uriHandler = LocalUriHandler.current,
        navigator = LocalNavigator.currentOrThrow,
    ),
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    getContentHandling: (BskyPost) -> List<ContentHandling> = { listOf() }
) {
    val padding = remember { when(role) {
        PostFragmentRole.Solo -> if(indentLevel == 0) Modifier.padding(2.dp) else Modifier
        PostFragmentRole.PrimaryThreadRoot -> Modifier.padding(2.dp)
        PostFragmentRole.ThreadBranchStart -> Modifier.padding(start = 0.dp, top = 0.dp, end = 2.dp, bottom = 2.dp)
        PostFragmentRole.ThreadBranchMiddle -> Modifier.padding(start = 0.dp, top = 0.dp, end = 2.dp, bottom = 2.dp)
        PostFragmentRole.ThreadBranchEnd -> Modifier.padding(start = 0.dp, top = 0.dp, end = 2.dp, bottom = 2.dp)
        PostFragmentRole.ThreadRootUnfocused -> Modifier.padding(2.dp)
        PostFragmentRole.ThreadEnd -> Modifier.padding(start = 2.dp, top = 0.dp, end = 2.dp, bottom = 2.dp)
    }}
    WrappedColumn(modifier = modifier.then(padding.fillMaxWidth())) {
        val delta = remember { getFormattedDateTimeSince(post.createdAt) }
        val indent = remember { when(role) {
            PostFragmentRole.Solo -> indentLevel.toFloat()
            PostFragmentRole.PrimaryThreadRoot -> indentLevel.toFloat()
            PostFragmentRole.ThreadBranchStart -> 0.0f//indentLevel.toFloat()
            PostFragmentRole.ThreadBranchMiddle -> 0.0f//indentLevel.toFloat()-1
            PostFragmentRole.ThreadBranchEnd -> 0.0f//indentLevel.toFloat()-1
            PostFragmentRole.ThreadRootUnfocused -> indentLevel.toFloat()
            PostFragmentRole.ThreadEnd -> 0.0f
        }}

        val interactionSource = remember { MutableInteractionSource() }
        val indication = remember { MorphoHighlightIndication() }
        val bgColor = if (role == PostFragmentRole.PrimaryThreadRoot) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.surfaceColorAtElevation(if (elevate ) 2.dp else
            if (indentLevel > 0) (indentLevel*2).dp else 0.dp)
        }

        val contentHandling = remember {
            if (post.author.mutedByMe) {
                getContentHandling(post) + ContentHandling(
                    scope = Blurs.CONTENT,
                    id = "muted",
                    icon = LabelIcon.EyeSlash(labelerAvatar = null),
                    action = LabelAction.Blur,
                    source = LabelDescription.YouMuted,
                )
            } else {
                getContentHandling(post)
            }.toImmutableList()
        }
        val onPostClicked: OnPostClicked = remember { { uri ->
            onItemClicked.onRichTextFacetClicked(uri = uri)
        } }
        val onFacetClicked: OnFacetClicked = remember { { facet ->
            onItemClicked.onRichTextFacetClicked(facet = facet)
        } }

        Surface (
            shadowElevation = if (elevate || indentLevel > 0) 2.dp else 0.dp,
            tonalElevation = if (elevate && role != PostFragmentRole.ThreadEnd) 2.dp
                else if (indentLevel > 0) (indentLevel*2).dp else 0.dp,
            shape = MaterialTheme.shapes.small,
            //color = bgColor,
            modifier = modifier
                .fillMaxWidth(indentLevel(indent))
                .align(Alignment.End)
                .clickable(
                    interactionSource = interactionSource,
                    indication = indication,
                    enabled = true,
                    onClick = { onPostClicked(post.uri) }
                )

        ) {
            ContentHider(
                reasons = contentHandling,
                scope = Blurs.CONTENT,
            ) {
                Row(
                    modifier = Modifier.padding(end = 6.dp)
                        .fillMaxWidth()//.fillMaxWidth(indentLevel(indent))
                ) {

                    if (indent < 2) {
                        OutlinedAvatar(
                            url = post.author.avatar.orEmpty(),
                            contentDescription = "Avatar for ${post.author.handle}",
                            size = 45.dp,
                            outlineColor = MaterialTheme.colorScheme.background,
                            onClicked = { onProfileClicked(post.author.did) },
                            avatarShape = AvatarShape.Corner,
                            modifier = Modifier.padding(end = 2.dp)
                        )
                    }

                    Column(
                        Modifier
                            .padding(top = 4.dp, start = 2.dp, end = 6.dp)
                            .fillMaxWidth()//.fillMaxWidth(indentLevel(indent))
                    ) {
                        if (post.reason is BskyPostReason.BskyPostRepost) {
                            Row(
                                modifier = Modifier.padding(start = 2.dp),
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
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(start = 5.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (indent >= 2) {
                                OutlinedAvatar(
                                    url = post.author.avatar.orEmpty(),
                                    contentDescription = "Avatar for ${post.author.handle}",
                                    size = 30.dp,
                                    avatarShape = AvatarShape.Rounded,
                                    outlineColor = MaterialTheme.colorScheme.background,
                                    onClicked = { onProfileClicked(post.author.did) }
                                )
                            }
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = MaterialTheme.typography.labelLarge.fontSize,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) {
                                        if (post.author.displayName != null) append("${post.author.displayName} ")
                                    }
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = MaterialTheme.typography.labelLarge.fontSize.times(
                                                0.8f
                                            )
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
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = indication,
                                        enabled = true,
                                        onClick = { onProfileClicked(post.author.did) }
                                    )
                            )

                            Spacer(modifier = Modifier.width(1.dp).weight(0.1F))
                            Text(
                                text = delta,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = MaterialTheme.typography.labelLarge.fontSize.div(1.2F),
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.End)
                                    //.weight(3.0F)
                                    .alignByBaseline(),
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                softWrap = false,
                            )
                        }

                        if (post.reply?.parentPost != null) {
                            ReplyIndicator(post.reply.parentPost)
                        }

                        if (post.facets.fastAny {
                                it.facetType.first() is FacetType.PollBlueOption
                            }) {
                            PollBluePost(
                                text = post.text,
                                facets = post.facets,
                                //modifier = Modifier.padding(bottom = 2.dp).padding(start = 0.dp, end = 6.dp),
                                onItemClicked = { onPostClicked(post.uri) },
                                onProfileClicked = onProfileClicked,
                                getContentHandling = getContentHandling
                            )
                        } else {
                            RichTextElement(
                                text = post.text,
                                facets = post.facets,
                                modifier = Modifier.padding(end = 2.dp),
                                onClick = { facetTypes ->
                                    if (facetTypes.isEmpty()) {
                                        onPostClicked(post.uri)
                                        return@RichTextElement
                                    }
                                    facetTypes.forEach {
                                        onFacetClicked(it)
                                    }
                                },
                            )
                        }
                        PostFeatureElement(
                            post.feature, onItemClicked, contentHandling = contentHandling
                        )

                        PostActions(
                            post = post,
                            onLikeClicked = { onLikeClicked(StrongRef(post.uri, post.cid)) },
                            onMenuClicked = { onMenuClicked(it, post) },
                            onReplyClicked = { onReplyClicked(post) },
                            onRepostClicked = { onRepostClicked(post) },
                            onUnClicked = onUnClicked,
                        )
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal inline fun ReplyIndicator(
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
            text = stringResource(Res.string.replyIndicator, parent.author.handle),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}

@Composable
inline fun ColumnScope.PostFeatureElement(
    feature: BskyPostFeature? = null,
    onItemClicked: OnItemClicked = ItemClicked(
        uriHandler = LocalUriHandler.current,
        navigator = LocalNavigator.currentOrThrow,
    ),
    crossinline onLikeClicked: (StrongRef) -> Unit = { },
    crossinline onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    contentHandling: List<ContentHandling> = listOf()
) {

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    when (feature) {
        is BskyPostFeature.ExternalFeature -> {
            val uriHandler = LocalUriHandler.current
            PostLinkEmbed(
                linkData = feature,
                linkPress = { openBrowser(it, uriHandler) },
                modifier = Modifier.padding(end = 2.dp)
                    .align(Alignment.CenterHorizontally)
            ) }
        is BskyPostFeature.ImagesFeature -> {
            ContentHider(
                reasons = contentHandling,
                scope = Blurs.MEDIA,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                PostImages(imagesFeature = feature,
                           modifier = Modifier.padding(end = 2.dp)
                               .align(Alignment.CenterHorizontally))
            }
        }
        is BskyPostFeature.MediaRecordFeature -> {
            ContentHider(
                reasons = contentHandling,
                scope = Blurs.MEDIA,
            ) {
                RecordFeature(
                    record = feature.record,
                    media = feature.media,
                    onItemClicked = onItemClicked,
                    onLikeClicked = onLikeClicked,
                    onUnClicked = onUnClicked,
                    contentHandling = contentHandling
                )
            }
        }
        is BskyPostFeature.RecordFeature -> {
            ContentHider(
                reasons = contentHandling,
                scope = Blurs.MEDIA,
            ) {
                RecordFeature(
                    record = feature.record,
                    onItemClicked = onItemClicked,
                    onLikeClicked = onLikeClicked,
                    onUnClicked = onUnClicked,
                    contentHandling = contentHandling
                )
            }
        }
        is BskyPostFeature.VideoFeature -> {
            ContentHider(
                reasons = contentHandling,
                scope = Blurs.MEDIA,
            ) {
                VideoEmbedThumb(
                    video = feature.video,
                    alt = feature.alt,
                    aspectRatio = feature.aspectRatio,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

        }
        is BskyPostFeature.UnknownEmbed -> {
            Text(text = "Unknown Embed ${feature.value}")
        }
        null -> {}

        else -> {Text(text = "Feature type not supported")}
    }
}

@Composable
inline fun ColumnScope.RecordFeature(
    record: EmbedRecord? = null,
    media: TimelinePostMedia? = null,
    onItemClicked: OnItemClicked = ItemClicked(
        uriHandler = LocalUriHandler.current,
        navigator = LocalNavigator.currentOrThrow,
    ),
    crossinline onLikeClicked: (StrongRef) -> Unit = { },
    crossinline onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    contentHandling: List<ContentHandling> = listOf(),
    getContentHandling: (EmbedRecord) -> List<ContentHandling> = { listOf() }
) {

    if(media != null) {

        ContentHider(
            reasons = contentHandling,
            scope = Blurs.MEDIA,
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            when(media) {
                is BskyPostFeature.ExternalFeature -> {
                    val uriHandler = LocalUriHandler.current
                    PostLinkEmbed(
                        linkData = media,
                        linkPress = { openBrowser(it, uriHandler) },
                        modifier = Modifier.padding(end = 2.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                is BskyPostFeature.ImagesFeature -> {
                    PostImages(
                        imagesFeature = media,
                        modifier = Modifier.padding(end = 2.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                is BskyPostFeature.VideoFeature -> {
                    VideoEmbedThumb(
                        video = media.video,
                        alt = media.alt,
                        aspectRatio = media.aspectRatio,
                        modifier = Modifier.padding(end = 2.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
                else -> {Text(text = "Record Feature not supported")}
            }
        }
    }
    if(record != null) {
        ContentHider(
            reasons = contentHandling,
            scope = Blurs.CONTENT,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            when (record) {
                is EmbedRecord.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = record.uri)
                is EmbedRecord.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = record.uri)
                is EmbedRecord.VisibleEmbedPost -> EmbedPostFragment(
                    post = record,
                    onItemClicked = { onItemClicked.onRichTextFacetClicked(uri = record.uri) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                is EmbedRecord.EmbedFeed -> {
                    FeedListEntryFragment(
                        record.feed,
                        likeClicked = { ref, liked ->
                            if (liked) onLikeClicked(ref)
                            else onUnClicked(RecordType.Like, ref.uri)
                        },
                        onFeedClicked = { }
                    )
                }
                is EmbedRecord.EmbedList -> {
                    UserListEntryFragment(
                        list = record.list,
                        onListClicked = { }
                    )
                }

                else -> {
                    Text(text = "Record Media Feature not supported")
                }
            }
        }
    }

}

