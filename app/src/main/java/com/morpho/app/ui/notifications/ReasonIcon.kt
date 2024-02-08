package morpho.app.ui.notifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.bsky.notification.ListNotificationsReason

@Composable
fun ReasonIcon(
    reason: ListNotificationsReason,
    modifier: Modifier = Modifier
) {
    when(reason) {
        ListNotificationsReason.LIKE -> Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Like",
            modifier = modifier
        )
        ListNotificationsReason.REPOST -> Icon(
            imageVector = Icons.Default.Repeat,
            contentDescription = "Repost",
            modifier = modifier
        )
        ListNotificationsReason.FOLLOW -> Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = "Follow",
            modifier = modifier
        )
        ListNotificationsReason.MENTION -> Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Mention",
            modifier = modifier
        )
        ListNotificationsReason.REPLY -> Icon(
            imageVector = Icons.AutoMirrored.Default.Reply,
            contentDescription = "Reply",
            modifier = modifier
        )
        ListNotificationsReason.QUOTE -> Icon(
            imageVector = Icons.Default.Repeat,
            contentDescription = "Quote",
            modifier = modifier
        )
    }
}