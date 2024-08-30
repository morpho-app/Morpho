package com.morpho.app.ui.post


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.morpho.app.ui.elements.*
import com.morpho.app.util.openBrowser
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi

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
    val maybeMuted = remember { if (post.author.mutedByMe) MutePersonDescribed else NoDescribed }


    WrappedColumn(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 4.dp)
            .padding(start = 6.dp, end = 6.dp)
    ) {
        ContentHider(
            reasons = persistentListOf(maybeMuted),
            scope = LabelScope.Content,
        ) {
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
