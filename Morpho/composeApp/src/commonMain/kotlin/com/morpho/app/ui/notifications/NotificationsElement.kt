package com.morpho.app.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.atproto.repo.StrongRef
import com.morpho.app.data.NotificationsListItem
import com.morpho.app.model.bluesky.BskyNotification
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.ui.common.OnPostClicked
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.post.PostFragment
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
    onPostClicked: OnPostClicked,
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
    Column {
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
                            markRead(item.notifications.first().uri)
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
                        onItemClicked = {
                            if(!readOnLoad) markRead(item.notifications.first().uri)
                            onPostClicked(it)
                                        },
                        onProfileClicked = {
                            if(!readOnLoad) markRead(item.notifications.first().uri)
                            scope.launch {
                                resolveHandle(it)?.let { did -> onAvatarClicked(did) }
                            }
                        },
                        onUnClicked = { type, uri ->
                            if(!readOnLoad) markRead(item.notifications.first().uri)
                            onUnClicked(type, uri)
                        },
                        onRepostClicked = {
                            onRepostClicked(it)
                            if(!readOnLoad) markRead(item.notifications.first().uri)
                        },
                        onReplyClicked = {
                            onReplyClicked(it)
                            if(!readOnLoad) markRead(item.notifications.first().uri)
                        },
                        onMenuClicked = { option, p ->
                            onMenuClicked(option, p)
                            if(!readOnLoad) markRead(item.notifications.first().uri)
                        },
                        onLikeClicked = {
                            onLikeClicked(it)
                            if(!readOnLoad) markRead(item.notifications.first().uri)
                                        },
                    )
                }
            }
        }
        HorizontalDivider(Modifier.fillMaxWidth().padding(top = 2.dp),thickness = Dp.Hairline)
    }
}