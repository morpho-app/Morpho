package com.morpho.app.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
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
import com.morpho.app.model.bluesky.NotificationsListItem
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.butterfly.Did
import kotlin.math.min

@Composable
fun NotificationAvatarList(
    item: NotificationsListItem,
    modifier: Modifier = Modifier,
    onClicked: (Did) -> Unit = {},
    unread: Boolean = false
) {
    var expand by remember { mutableStateOf(false) }
    Column(
        Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!expand) {
                if (item.notifications.size > 1) {
                    item.notifications.subList(0, min(7, item.notifications.size)).forEach {
                        OutlinedAvatar(
                            url = it.author.avatar.orEmpty(),
                            onClicked = { onClicked(it.author.did) },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                } else {
                    OutlinedAvatar(
                        url = item.notifications.first().author.avatar.orEmpty(),
                        onClicked = { onClicked(item.notifications.first().author.did) },
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
            if (item.notifications.size > 1) {
                Spacer(modifier = Modifier.width(1.dp).weight(0.1f))
                IconToggleButton(
                    checked = expand,
                    onCheckedChange = {
                        expand = it
                    },
                    modifier = Modifier
                        .size(30.dp)
                        .padding(end = 8.dp)
                ) {
                    if (expand) {
                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = "Hide Details"
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Show Details"
                        )
                    }

                }
            }
        }
        if (expand) {
            item.notifications.forEach {
                Row(Modifier.padding(vertical = 2.dp)) {
                    OutlinedAvatar(
                        url = it.author.avatar.orEmpty(),
                        onClicked = { onClicked(it.author.did) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                        .times(1.2f),
                                    fontWeight = if(unread) FontWeight.Bold else FontWeight.Medium
                                )
                            ) {
                                if (it.author.displayName != null) append("${it.author.displayName} ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                        .times(1.0f),
                                    fontWeight = if(unread) FontWeight.SemiBold else FontWeight.Normal
                                )
                            ) {
                                append("@${it.author.handle}")
                            }
                        },
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(4.dp)
                            .alignByBaseline()
                            .clickable { onClicked(it.author.did) },
                    )
                }
            }
        }
    }
}