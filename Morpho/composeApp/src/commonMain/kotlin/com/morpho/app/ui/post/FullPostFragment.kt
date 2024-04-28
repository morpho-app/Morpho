package com.morpho.app.ui.post


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreHoriz
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
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.FacetType
import com.morpho.app.ui.elements.*
import com.morpho.app.ui.theme.MorphoTheme
import com.morpho.app.util.openBrowser
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import morpho.composeapp.generated.resources.Res
import morpho.composeapp.generated.resources.hideMutedPost
import morpho.composeapp.generated.resources.showMutedPost
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class)
@Composable
fun FullPostFragment(
    post: BskyPost,
    modifier: Modifier = Modifier,
    onItemClicked: (AtUri) -> Unit = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    ) {
    val postDate = remember { post.createdAt.instant.toLocalDateTime(TimeZone.currentSystemDefault()).date }
    var menuExpanded by remember { mutableStateOf(false) }
    var hidePost by rememberSaveable { mutableStateOf(post.author.mutedByMe) }

    WrappedColumn(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 4.dp)
            .padding(start = 6.dp, end = 6.dp)
    ) {
        if(post.author.mutedByMe) {
            TextButton(
                onClick = { hidePost = !hidePost },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (hidePost) {
                        Icons.AutoMirrored.Default.NavigateNext
                    } else {
                        Icons.Default.ExpandMore
                    },
                    contentDescription = null)
                Spacer(modifier = Modifier
                    .width(1.dp)
                    .weight(0.3f))
                Text(
                    text = if(hidePost) {
                        stringResource(Res.string.showMutedPost)
                    } else {
                        stringResource(Res.string.hideMutedPost)
                    }
                )
            }
        }
        if(!hidePost) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.End

            ) {
                OutlinedAvatar(
                    url = post.author.avatar.orEmpty(),
                    contentDescription = "Avatar for ${post.author.handle}",
                    modifier = Modifier
                        .size(55.dp)
                        .align(Alignment.CenterVertically),
                    outlineColor = MaterialTheme.colorScheme.background,
                    onClicked = { onProfileClicked(post.author.did) }
                )
                SelectionContainer(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp)
                        .clickable { onProfileClicked(post.author.did) },
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
                                append("\n@${post.author.handle}")
                            }

                        },
                        maxLines = 2,
                        style = MaterialTheme.typography.labelLarge,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .wrapContentWidth(Alignment.Start)
                            .alignByBaseline()
                            .align(Alignment.CenterVertically).clickable { onProfileClicked(post.author.did) }
                    )
                }
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(0.1F),
                )
                IconButton(onClick = { menuExpanded= !menuExpanded }) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                PostMenu(menuExpanded, {
                    onMenuClicked(it, post)
                }, onDismissRequest = { menuExpanded = false })
            }


            RichTextElement(
                text = post.text,
                facets = post.facets,
                onClick = {
                    when (it) {
                        is FacetType.ExternalLink -> { openBrowser(it.uri.uri) }
                        is FacetType.Format -> { onItemClicked(post.uri) }
                        is FacetType.PollBlueOption -> {}
                        is FacetType.Tag -> { onItemClicked(post.uri) }
                        is FacetType.UserDidMention -> { onProfileClicked(post.author.did) }
                        is FacetType.UserHandleMention -> { onProfileClicked(it.handle) }
                        null -> { onItemClicked(post.uri) }
                        else -> {}
                    }
                }
            )
            val postTimestamp = remember {
                val seconds = post.createdAt.instant.epochSeconds % 60
                Instant.fromEpochSeconds(post.createdAt.instant.epochSeconds - seconds)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).time
            }

            PostFeatureElement(post.feature, onItemClicked)

            Row(verticalAlignment = Alignment.CenterVertically) {
                PostActions(
                    post = post,
                    showMenu = false,
                    onLikeClicked = { onLikeClicked(StrongRef(post.uri, post.cid)) },
                    onReplyClicked = { onReplyClicked(post) },
                    onRepostClicked = { onRepostClicked(post) },
                    onUnClicked = onUnClicked,
                )
                Spacer(Modifier.width(1.dp).weight(0.1F))
                SelectionContainer(Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
                    Text(
                        text = "$postDate at $postTimestamp",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = MaterialTheme.typography.labelLarge
                            .fontSize.div(1.2F),
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewFullPostFragment() {
    MorphoTheme(darkTheme = false) {
        Column (modifier = Modifier.fillMaxWidth()
        ){
            FullPostFragment(
                post = testThreadRoot,
                onItemClicked = {},
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}