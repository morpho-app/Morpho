package radiant.nimbus.ui.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import radiant.nimbus.api.AtUri
import radiant.nimbus.model.BskyNotification
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.NotificationsListItem
import radiant.nimbus.ui.post.PostFragment
import radiant.nimbus.util.getFormattedDateTimeSince

@Composable
fun NotificationsElement(
    item: NotificationsListItem,
    showPost: Boolean = true,
    getPost: suspend (AtUri) -> Deferred<BskyPost?>,
) {
    var expand by remember { mutableStateOf(showPost) }
    var post: BskyPost? by remember { mutableStateOf(null) }
    val delta = remember { getFormattedDateTimeSince(item.notifications.first().indexedAt) }
    LaunchedEffect(expand) {
        when (val notif = item.notifications.first()) {
            is BskyNotification.Like -> post = getPost(notif.subject.uri).await()
            is BskyNotification.Follow -> {}
            is BskyNotification.Post -> {
                post = notif.post
                launch {
                    post = getPost(notif.uri).await()
                }
            }

            is BskyNotification.Repost -> post = getPost(notif.subject.uri).await()
            is BskyNotification.Unknown -> {
                if (notif.reasonSubject != null) {
                    post = getPost(notif.reasonSubject!!).await()
                }
            }
        }
    }
    val firstName = remember {
        if (item.notifications.first().author.displayName?.isNotEmpty() == true) {
            item.notifications.first().author.displayName.orEmpty()
        } else {
            item.notifications.first().author.handle.handle
        }
    }
    val number = remember { item.notifications.size }
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
            )
            NotificationText(reason = item.reason, number = number, name = firstName, delta = delta)
            if (expand && post != null) {
                // TODO: maybe do a more compact variant
                PostFragment(
                    post = post!!, elevate = true,
                )
            }
        }

    }
}