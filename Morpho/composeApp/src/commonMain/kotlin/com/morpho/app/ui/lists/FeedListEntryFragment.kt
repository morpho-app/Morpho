package com.morpho.app.ui.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.FacetType
import com.morpho.app.model.bluesky.FeedGenerator
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.RichTextElement
import com.morpho.app.util.openBrowser

@Composable
fun FeedListEntryFragment(
    feed: FeedGenerator,
    modifier: Modifier = Modifier,
    hasFeedSaved: Boolean = false,
    likeClicked: (StrongRef, Boolean) -> Unit = {_,_->},
    saveFeedClicked: (StrongRef, Boolean) -> Unit = {_,_->},
    onFeedClicked: (FeedGenerator) -> Unit = {},
) {
    var saved by remember { mutableStateOf(hasFeedSaved) }
    var liked by remember { mutableStateOf(feed.likedByMe) }
    var numLikes by remember { mutableStateOf(feed.likeCount)}
    Surface (
        shadowElevation = 1.dp,
        tonalElevation =  4.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()

    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clickable { onFeedClicked(feed) }
                .padding(bottom = 4.dp)
                .padding(start = 0.dp, end = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(end = 4.dp),
                horizontalArrangement = Arrangement.End

            ) {
                OutlinedAvatar(
                    url = feed.avatar.orEmpty(),
                    contentDescription = "Avatar for ${feed.displayName}",
                    modifier = Modifier
                        .size(55.dp)
                        .align(Alignment.CenterVertically),
                    outlineColor = MaterialTheme.colorScheme.tertiary,
                    onClicked = { onFeedClicked(feed) }
                )
                SelectionContainer(
                    modifier = Modifier
                        //.padding(bottom = 12.dp
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp, top = 4.dp)
                        .clickable { onFeedClicked(feed) },
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
                                append(feed.creator.displayName.orEmpty())
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                        .times(1.0f)
                                )
                            ) {
                                append("\n@${feed.creator.handle}")
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
                IconButton(
                    onClick = {
                        saved = !saved
                        saveFeedClicked(StrongRef(feed.uri, feed.cid), saved)
                    },
                ) {
                    Icon(
                        imageVector = if (saved) Icons.Default.DeleteOutline else Icons.Default.Add,
                        contentDescription = if(saved) "Remove from my Feeds" else "Add to my Feeds",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            RichTextElement(
                text = feed.description.orEmpty(),
                facets = feed.descriptionFacets,
                onClick = { facetTypes ->
                    if (facetTypes.isEmpty()) {
                        onFeedClicked(feed)
                        return@RichTextElement
                    }
                    facetTypes.fastForEach { facetType ->
                        when (facetType) {
                            is FacetType.ExternalLink -> { openBrowser(facetType.uri.uri) }
                            is FacetType.Format -> {  }
                            is FacetType.PollBlueOption -> {}
                            is FacetType.Tag -> { }
                            is FacetType.UserDidMention -> { }
                            is FacetType.UserHandleMention -> { }
                            else -> {}
                        }
                    }
                },
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
            ) {
                Text(
                    text = "Liked by $numLikes users",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                    style = MaterialTheme.typography.labelMedium,
                )

                IconButton(
                    onClick = {
                        liked = !liked
                        if (!liked) numLikes-- else numLikes++
                        likeClicked(StrongRef(feed.uri, feed.cid), liked)
                    }
                ) {
                    Icon(
                        imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if(liked) "Like feed" else "Unlike feed",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}