package radiant.nimbus.ui.post

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.atproto.repo.StrongRef
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.BskyPostFeature
import radiant.nimbus.model.EmbedPost
import radiant.nimbus.ui.elements.MenuOptions
import radiant.nimbus.ui.elements.OutlinedAvatar
import radiant.nimbus.ui.elements.PostMenu
import radiant.nimbus.ui.theme.NimbusTheme
import radiant.nimbus.ui.utils.DevicePreviews
import radiant.nimbus.ui.utils.FontScalePreviews
import radiant.nimbus.util.getFormattedDateTimeSince
import radiant.nimbus.util.getRkey

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullPostFragment(
    post: BskyPost,
    modifier: Modifier = Modifier,
    onItemClicked: (AtUri) -> Unit = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (StrongRef) -> Unit = { },
    onRepostClicked: (StrongRef) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, rkey: String) -> Unit = { _, _ -> },
    lkeyFlow: Flow<String?> = flowOf(getRkey(post.likeUri)),
    rpkeyFlow: Flow<String?> = flowOf(getRkey(post.repostUri))
    ) {
    val delta = rememberSaveable { getFormattedDateTimeSince(post.createdAt) }
    val timestamp =
        remember { post.createdAt.instant.toLocalDateTime(TimeZone.currentSystemDefault()).time }
    val postDate =
        remember { post.createdAt.instant.toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val lineColour = MaterialTheme.colorScheme.onSurfaceVariant
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val diff = remember { today.toEpochDays() - postDate.toEpochDays() }
    var menuExpanded by remember { mutableStateOf(false) }

    val ctx = LocalContext.current

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 4.dp)
            .padding(start = 6.dp, end = 6.dp)
            .clickable {}
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
                    .size(50.dp)
                    .align(Alignment.CenterVertically),
                outlineColor = MaterialTheme.colorScheme.background,
                onClicked = { onProfileClicked(AtIdentifier(post.author.did.did)) }
            )
            SelectionContainer(
                modifier = Modifier
                    //.padding(bottom = 12.dp
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
                        //.padding(bottom = 12.dp)
                        .alignByBaseline()
                        .align(Alignment.CenterVertically)
                    //.padding(start = 16.dp),

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


        SelectionContainer(
            modifier = Modifier
                .padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                .clickable { onItemClicked(post.uri) }
        ) {
            MarkdownText(
                markdown = post.text.replace("\n", "  \n"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                //disableLinkMovementMethod = true,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
                onLinkClicked = {
                    val urlIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(it)
                    )
                    ctx.startActivity(urlIntent)
                }
            )
        }
        val postTimestamp = remember {
            val seconds = post.createdAt.instant.epochSeconds % 60
            Instant.fromEpochSeconds(post.createdAt.instant.epochSeconds - seconds)
                .toLocalDateTime(TimeZone.currentSystemDefault()).time
        }
        when (post.feature) {
            is BskyPostFeature.ExternalFeature -> PostLinkEmbed(
                linkData = post.feature,
                linkPress = {
                    val urlIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(it)
                    )
                    ctx.startActivity(urlIntent)
                }
            )

            is BskyPostFeature.ImagesFeature -> PostImages(imagesFeature = post.feature)
            is BskyPostFeature.MediaPostFeature -> {
                when (post.feature.media) {
                    is BskyPostFeature.ExternalFeature -> PostLinkEmbed(
                        linkData = post.feature.media,
                        linkPress = {
                            val urlIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(it)
                            )
                            ctx.startActivity(urlIntent)
                        }
                    )

                    is BskyPostFeature.ImagesFeature -> PostImages(imagesFeature = post.feature.media)
                }
                when (post.feature.post) {
                    is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = post.feature.post.uri)
                    is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = post.feature.post.uri)
                    is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(
                        post = post.feature.post,
                        onItemClicked = { onItemClicked(it) }
                    )
                }
            }

            is BskyPostFeature.PostFeature -> {
                when (post.feature.post) {
                    is EmbedPost.BlockedEmbedPost -> EmbedBlockedPostFragment(uri = post.feature.post.uri)
                    is EmbedPost.InvisibleEmbedPost -> EmbedNotFoundPostFragment(uri = post.feature.post.uri)
                    is EmbedPost.VisibleEmbedPost -> EmbedPostFragment(
                        post = post.feature.post,
                        onItemClicked = { onItemClicked(it) }
                    )
                }
            }

            null -> {}
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            PostActions(
                post = post,
                showMenu = false,
                onLikeClicked = {
                    onLikeClicked(StrongRef(post.uri, post.cid))
                },
                onReplyClicked = {

                },
                onRepostClicked = {
                    onRepostClicked(StrongRef(post.uri, post.cid))
                },
                onUnClicked = onUnClicked,
                lkeyFlow = lkeyFlow,
                rpkeyFlow = rpkeyFlow,
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

@DevicePreviews
@FontScalePreviews
@Composable
fun PreviewFullPostFragment() {
    NimbusTheme(darkTheme = false) {
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