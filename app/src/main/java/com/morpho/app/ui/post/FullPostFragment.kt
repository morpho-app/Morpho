package morpho.app.ui.post

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import morpho.app.api.AtIdentifier
import morpho.app.api.AtUri
import morpho.app.api.model.RecordType
import morpho.app.model.BskyPost
import morpho.app.model.FacetType
import morpho.app.ui.elements.MenuOptions
import morpho.app.ui.elements.OutlinedAvatar
import morpho.app.ui.elements.PostMenu
import morpho.app.ui.elements.RichTextElement
import morpho.app.ui.theme.MorphoTheme
import morpho.app.ui.utils.DevicePreviews
import morpho.app.ui.utils.FontScalePreviews

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullPostFragment(
    post: BskyPost,
    modifier: Modifier = Modifier,
    onItemClicked: (AtUri) -> Unit = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    ) {
    val postDate = remember { post.createdAt.instant.toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val diff = rememberSaveable { today.toEpochDays() - postDate.toEpochDays() }
    var menuExpanded by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    var hidePost by rememberSaveable { mutableStateOf(post.author.mutedByMe) }

    Column(
        Modifier
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
                        "Show post by muted author"
                    } else {
                        "Hide post by muted author"
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
                    onClicked = { onProfileClicked(AtIdentifier(post.author.did.did)) }
                )
                SelectionContainer(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp)
                        .clickable { onProfileClicked(AtIdentifier(post.author.did.did)) },
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
                            .align(Alignment.CenterVertically)
                    )
                }
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(0.1F),
                )
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                PostMenu(menuExpanded, onMenuClicked, onDismissRequest = { menuExpanded = false })
            }


            RichTextElement(
                text = post.text,
                facets = post.facets,
                onClick = {
                    when (it) {
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

                        is FacetType.Tag -> {
                            onItemClicked(post.uri)
                        }
                        is FacetType.UserDidMention -> {
                            onProfileClicked(AtIdentifier(it.did.did))
                        }

                        is FacetType.UserHandleMention -> {
                            onProfileClicked(AtIdentifier(it.handle.handle))
                        }

                        null -> {
                            onItemClicked(post.uri)
                        }
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
                    onLikeClicked = {
                        onLikeClicked(StrongRef(post.uri, post.cid))
                    },
                    onReplyClicked = {
                        onReplyClicked(post)
                    },
                    onRepostClicked = {
                        onRepostClicked(post)
                    },
                    onUnClicked = onUnClicked,
                )
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(0.1F),
                )
                SelectionContainer(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                ) {
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

@DevicePreviews
@FontScalePreviews
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