package com.morpho.app.ui.lists


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RssFeed
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
import app.bsky.graph.ListType
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyList
import com.morpho.app.model.bluesky.FacetType
import com.morpho.app.model.bluesky.UserList
import com.morpho.app.model.bluesky.UserListBasic
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.RichTextElement
import com.morpho.app.util.openBrowser
import kotlinx.collections.immutable.persistentListOf

@Composable
fun UserListEntryFragment(
    list: BskyList,
    modifier: Modifier = Modifier,
    hasListPinned: Boolean = false,
    muteListClicked: (StrongRef, Boolean) -> Unit = {_,_->},
    blockListClicked: (StrongRef, Boolean) -> Unit = {_,_->},
    pinListClicked: (StrongRef, Boolean) -> Unit = {_,_->},
    onListClicked: (BskyList) -> Unit = {},
) {
    var pinned by remember { mutableStateOf(hasListPinned) }
    var muted by remember { mutableStateOf(list.viewerMuted) }
    var blocked by remember { mutableStateOf(list.viewerBlocked != null)}
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
                .clickable { onListClicked(list) }
                .padding(bottom = 4.dp)
                .padding(start = 0.dp, end = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(end = 4.dp),
                horizontalArrangement = Arrangement.End

            ) {
                if(list.avatar != null) {
                    OutlinedAvatar(
                        url = list.avatar.orEmpty(),
                        contentDescription = "Avatar for ${list.name}",
                        modifier = Modifier
                            .size(55.dp)
                            .align(Alignment.CenterVertically),
                        outlineColor = MaterialTheme.colorScheme.tertiary,
                        onClicked = { onListClicked(list) }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RssFeed,
                        contentDescription = "Avatar for ${list.name}",
                        modifier = Modifier
                            .size(55.dp)
                            .align(Alignment.CenterVertically),
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
                SelectionContainer(
                    modifier = Modifier
                        //.padding(bottom = 12.dp
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp, top = 4.dp)
                        .clickable { onListClicked(list) },
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
                                append(when(list) {
                                    is UserList -> list.creator.displayName.orEmpty()
                                    is UserListBasic -> ""
                                })
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                        .times(1.0f)
                                )
                            ) {
                                append(when(list) {
                                   is UserList -> list.creator.handle.handle
                                   is UserListBasic -> ""
                                })
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
                if(list.purpose == ListType.CURATELIST) {
                    IconButton(
                        onClick = {
                            pinned = !pinned
                            pinListClicked(StrongRef(list.uri, list.cid), pinned)
                        },
                    ) {
                        Icon(
                            imageVector = if (pinned) Icons.Default.DeleteOutline else Icons.Default.PushPin,
                            contentDescription = if(pinned) "Unpin from my feeds" else "Pin as a feed",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    TextButton(
                        onClick = {
                            muted = !muted
                            muteListClicked(StrongRef(list.uri, list.cid), muted)
                        },
                    ) {
                        Text(
                            text = if(muted) "Unmute" else "Mute",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    TextButton(
                        onClick = {
                            blocked = !blocked
                            blockListClicked(StrongRef(list.uri, list.cid), blocked)
                        },
                    ) {
                        Text(
                            text = if(blocked) "Unblock" else "Block",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            RichTextElement(
                text = when(list) {
                    is UserList -> list.description.orEmpty()
                    is UserListBasic -> ""
                },
                facets = when(list) {
                    is UserList -> list.descriptionFacets
                    is UserListBasic -> persistentListOf()
                },
                onClick = { facetTypes ->
                    if (facetTypes.isEmpty()) {
                        onListClicked(list)
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
        }
    }
}