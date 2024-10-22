package com.morpho.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyNotification
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.NotificationsListItem
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.post.PostFragment
import com.morpho.app.ui.utils.ItemClicked
import com.morpho.app.util.getFormattedDateTimeSince
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Did
import com.morpho.butterfly.model.RecordType
import kotlinx.coroutines.launch

@Composable
fun NotificationsElement(
    item: NotificationsListItem,
    showPost: Boolean = true,
    getPost: suspend (AtUri) -> BskyPost?,
    onItemClicked: ItemClicked = ItemClicked(
        uriHandler = LocalUriHandler.current,
        navigator = LocalNavigator.currentOrThrow,
    ),
    onAvatarClicked: (Did) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    readOnLoad: Boolean = false,
    markRead: (AtUri) -> Unit = {  },
    resolveHandle: suspend (AtIdentifier) -> Did?,
) {
    var expand by remember { mutableStateOf(showPost) }
    var post: BskyPost? by remember { mutableStateOf(null) }
    val delta = remember { getFormattedDateTimeSince(item.notifications.first().indexedAt) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(expand) {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        when (val notif = item.notifications.first()) {
            is BskyNotification.Like -> {
                if(showPost) post = getPost(notif.subject.uri)
            }
            is BskyNotification.Follow -> {}
            is BskyNotification.Post -> {
                post = notif.post
                if(showPost) post = getPost(notif.uri)
            }

            is BskyNotification.Repost -> {
                if(showPost) post = getPost(notif.subject.uri)
            }
            is BskyNotification.Unknown -> {
                if (notif.reasonSubject != null && showPost) {
                    post = getPost(notif.reasonSubject!!)
                }
            }

            else -> {}
        }
    }
    var unread by remember { mutableStateOf(item.notifications.any { !it.isRead }) }
    val markAsRead: (AtUri) -> Unit = remember { { uri ->
        markRead(uri)
        unread = false
    } }

    remember {
        if (!readOnLoad) return@remember
        // We just mark the first notification as read,
        // because that propagates to the rest and is slightly faster
        markRead(item.notifications.first().uri)
    }
    val firstName = remember {
        if (item.notifications.first().author.displayName?.isNotEmpty() == true) {
            item.notifications.first().author.displayName.orEmpty()
        } else {
            item.notifications.first().author.handle.handle
        }
    }
    val number = remember { item.notifications.size }
    Column(
        modifier = if(unread) Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            .clickable { markAsRead(item.notifications.first().uri) }
            else Modifier.clickable { markAsRead(item.notifications.first().uri) }
    ) {
        Row(
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ReasonIcon(
                    reason = item.reason,
                    Modifier.padding(top = 8.dp)
                )
                if (post != null) {
                    if (expand) {
                        Spacer(Modifier.height(20.dp))
                    } else {
                        Spacer(Modifier.height(4.dp))
                    }
                    IconToggleButton(
                        checked = expand,
                        onCheckedChange = {
                            expand = it
                            markAsRead(item.notifications.first().uri)
                        },
                    ) {
                        if (expand) {
                            Icon(
                                imageVector = Icons.Default.ExpandLess,
                                contentDescription = "Hide Post"
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Show Post"
                            )
                        }
                    }
                }
            }

            Column(
                Modifier
                    .padding(end = 4.dp)
                    .wrapContentSize(Alignment.TopStart)
            ) {
                NotificationAvatarList(
                    item = item,
                    onClicked = { onAvatarClicked(it) }
                )
                NotificationText(reason = item.reason, number = number, name = firstName, delta = delta)
                if (expand && post != null) {
                    // TODO: maybe do a more compact variant

                    PostFragment(
                        post = post!!, elevate = true,
                        onItemClicked = onItemClicked.copy(
                            callbackAlways = {
                                if(!readOnLoad) markAsRead(item.notifications.first().uri)
                            }
                        ),
                        onProfileClicked = {
                            if(!readOnLoad) markAsRead(item.notifications.first().uri)
                            scope.launch {
                                resolveHandle(it)?.let { did -> onAvatarClicked(did) }
                            }
                        },
                        onUnClicked = { type, uri ->
                            if(!readOnLoad) markAsRead(item.notifications.first().uri)
                            onUnClicked(type, uri)
                        },
                        onRepostClicked = {
                            onRepostClicked(it)
                            if(!readOnLoad) markAsRead(item.notifications.first().uri)
                        },
                        onReplyClicked = {
                            onReplyClicked(it)
                            if(!readOnLoad) markAsRead(item.notifications.first().uri)
                        },
                        onMenuClicked = { option, p ->
                            onMenuClicked(option, p)
                            if(!readOnLoad) markAsRead(item.notifications.first().uri)
                        },
                        onLikeClicked = {
                            onLikeClicked(it)
                            if(!readOnLoad) markAsRead(item.notifications.first().uri)
                        },
                    )
                }
            }
        }
        HorizontalDivider(Modifier.fillMaxWidth().padding(top = 2.dp),thickness = Dp.Hairline)
    }
}