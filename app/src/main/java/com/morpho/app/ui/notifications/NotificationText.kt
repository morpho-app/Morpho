package morpho.app.ui.notifications

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.bsky.notification.ListNotificationsReason

@Composable
fun NotificationText(
    reason: ListNotificationsReason,
    number: Int,
    name: String,
    delta: String,
    modifier: Modifier = Modifier,
) {
    val text = if (reason != ListNotificationsReason.REPLY && reason != ListNotificationsReason.QUOTE) {
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.labelLarge.fontSize,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append(name)
            }
            if (number > 1) {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = MaterialTheme.typography.labelLarge.fontSize
                    )
                ) {
                    append(" and ")
                }
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.labelLarge.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("${number - 1} other${if (number > 2) "s" else ""}")
                }
            }
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                )
            ) {
                when (reason) {
                    ListNotificationsReason.LIKE -> append(" liked your post  $delta")
                    ListNotificationsReason.REPOST -> append(" reposted your post  $delta")
                    ListNotificationsReason.FOLLOW -> append(" followed you  $delta")
                    ListNotificationsReason.MENTION -> append(" mentioned you  $delta")
                    else -> {}
                }
            }
        }
    } else null
    if (text != null) {
        Text(
            text = text,
            maxLines = 2,
            softWrap = true,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}