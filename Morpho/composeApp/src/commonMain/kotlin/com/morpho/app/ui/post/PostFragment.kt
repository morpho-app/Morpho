package com.morpho.app.ui.post


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
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
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.*
import com.morpho.app.ui.common.OnPostClicked
import com.morpho.app.ui.elements.*
import com.morpho.app.ui.lists.FeedListEntryFragment
import com.morpho.app.util.getFormattedDateTimeSince
import com.morpho.app.util.openBrowser
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import kotlinx.collections.immutable.persistentListOf
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
    onItemClicked: OnPostClicked = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
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
    WrappedColumn(modifier = padding.fillMaxWidth()) {
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
                ) }
            PostFragmentRole.ThreadBranchMiddle -> {
                baseShape.copy(
                    topEnd = CornerSize(0.dp),
                    topStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp),
                ) }
            PostFragmentRole.ThreadBranchEnd -> {
                baseShape.copy(
                    topEnd = CornerSize(0.dp),
                    topStart = CornerSize(0.dp),
                ) }
            PostFragmentRole.ThreadRootUnfocused -> baseShape
            PostFragmentRole.ThreadEnd -> {
                baseShape.copy(
                    topEnd = CornerSize(0.dp),
                    topStart = CornerSize(0.dp),
                ) }
        }
        val bgColor = if (role == PostFragmentRole.ThreadEnd) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.surfaceColorAtElevation(if (elevate || indentLevel > 0) 2.dp else 0.dp)
        }

        val maybeMuted = remember { if (post.author.mutedByMe) MutePersonDescribed else NoDescribed }

        Surface (
            shadowElevation = if (elevate || indentLevel > 0) 1.dp else 0.dp,
            tonalElevation = if ((elevate || indentLevel > 0) && role != PostFragmentRole.ThreadEnd) 2.dp else 0.dp,
            shape = shape,
            modifier = modifier
                .fillMaxWidth(indentLevel(indent))
                .align(Alignment.End)
                .background(bgColor, shape)

        ) {
            ContentHider(
                reasons = persistentListOf(maybeMuted),
                scope = LabelScope.Content,
            ) {
                SelectionContainer(
                    Modifier.clickable { onItemClicked(post.uri) }
                ) {
                    Row(
                        modifier = Modifier.padding(bottom = 2.dp).padding(start = 0.dp, end = 6.dp)
                            .fillMaxWidth(indentLevel(indent))
                    ) {

                        if (indent < 2) {
                            OutlinedAvatar(
                                url = post.author.avatar.orEmpty(),
                                contentDescription = "Avatar for ${post.author.handle}",
                                size = 45.dp,
                                outlineColor = MaterialTheme.colorScheme.background,
                                onClicked = { onProfileClicked(post.author.did) },
                                avatarShape = AvatarShape.Corner
                            )
                        }

                        Column(
                            Modifier
                                .padding(vertical = 2.dp, horizontal = 6.dp)
                                .fillMaxWidth(indentLevel(indent)),
                        ) {
                            if (post.reason is BskyPostReason.BskyPostRepost) {
                                Row(
                                    modifier = Modifier,
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
                                modifier = Modifier.padding(top = 4.dp).padding(horizontal = 4.dp),
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
                                                fontSize = MaterialTheme.typography.labelLarge.fontSize.times(
                                                    1.2f
                                                ),
                                                fontWeight = FontWeight.Medium
                                            )
                                        ) {
                                            if (post.author.displayName != null) append("${post.author.displayName} ")
                                        }
                                        withStyle(
                                            style = SpanStyle(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = MaterialTheme.typography.labelLarge.fontSize.times(
                                                    1.0f
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
                                        .clickable { onProfileClicked(post.author.did) },
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

                            if (post.reply?.parent != null) {
                                ReplyIndicator(post.reply.parent)
                            }

                            RichTextElement(
                                text = post.text,
                                facets = post.facets,
                                onClick = {
                                    when (it) {
                                        is FacetType.ExternalLink -> {
                                            openBrowser(it.uri.uri)
                                        }

                                        is FacetType.Format -> {
                                            onItemClicked(post.uri)
                                        }

                                        is FacetType.PollBlueOption -> {}
                                        is FacetType.Tag -> {
                                            onItemClicked(post.uri)
                                        }

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
    crossinline onLikeClicked: (StrongRef) -> Unit = { },
    crossinline onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
) {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    when (feature) {
        is BskyPostFeature.ExternalFeature -> PostLinkEmbed(linkData = feature,
            linkPress = { openBrowser(it) },
            modifier = Modifier.align(Alignment.CenterHorizontally))
        is BskyPostFeature.ImagesFeature -> {
            PostImages(imagesFeature = feature,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        is BskyPostFeature.MediaRecordFeature -> {
            RecordFeature(
                record = feature.record,
                media = feature.media,
                onItemClicked = onItemClicked,
                onLikeClicked = onLikeClicked,
                onUnClicked = onUnClicked
            )
        }
        is BskyPostFeature.RecordFeature -> {
            RecordFeature(
                record = feature.record,
                onItemClicked = onItemClicked,
                onLikeClicked = onLikeClicked,
                onUnClicked = onUnClicked
            )
        }
        null -> {}
        else -> {}
    }
}

@Composable
inline fun ColumnScope.RecordFeature(
    record: EmbedRecord? = null,
    media: TimelinePostMedia? = null,
    crossinline onItemClicked: OnPostClicked = {},
    crossinline onLikeClicked: (StrongRef) -> Unit = { },
    crossinline onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
) {
    if(media != null) {
        when(media) {
            is BskyPostFeature.ExternalFeature -> {
                PostLinkEmbed(
                    linkData = media,
                    linkPress = { openBrowser(it) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is BskyPostFeature.ImagesFeature -> {
                PostImages(imagesFeature = media,
                           modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            else -> {}
        }
    }
    if(record != null) {
        when (record) {
            is EmbedRecord.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = record.uri)
            is EmbedRecord.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = record.uri)
            is EmbedRecord.VisibleEmbedPost -> EmbedPostFragment(
                post = record,
                onItemClicked =  {onItemClicked(record.uri)},
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            is EmbedRecord.EmbedFeed -> {
                FeedListEntryFragment(
                    record.feed,
                    likeClicked = { ref, liked ->
                        if(liked) onLikeClicked(ref)
                        else onUnClicked(RecordType.Like, ref.uri)
                    },
                    onFeedClicked = {  }
                )
            }
            else -> {}
        }
    }

}

