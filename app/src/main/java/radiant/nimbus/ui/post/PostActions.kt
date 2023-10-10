package radiant.nimbus.ui.post

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.model.BskyPost
import radiant.nimbus.ui.elements.MenuOptions
import radiant.nimbus.ui.elements.PostMenu
import radiant.nimbus.util.getRkey

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostActions(
    post: BskyPost,
    modifier: Modifier = Modifier,
    showMenu: Boolean = true,
    onReplyClicked: () -> Unit = { },
    onRepostClicked: () -> Unit = { },
    onLikeClicked: () -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, rkey: String) -> Unit = { _, _ -> },
    lkeyFlow: Flow<String?> = flowOf(getRkey(post.likeUri)),
    rpkeyFlow: Flow<String?> = flowOf(getRkey(post.repostUri))
) {
    var menuExpanded by remember { mutableStateOf(false) }
    FlowRow {
        PostAction(
            parameter = post.replyCount,
            iconNormal = Icons.Outlined.ChatBubbleOutline,
            iconActive = Icons.Default.ChatBubble,
            contentDescription = "Reply",
            onClicked = onReplyClicked,
            //onUnClicked = { onUnClicked(RecordType.Post, it) },
        )
        PostAction(
            parameter = post.repostCount,
            iconNormal = Icons.Outlined.Repeat,
            contentDescription = "Repost",
            onClicked = onRepostClicked,
            onUnClicked = { onUnClicked(RecordType.Repost, it) },
            rkey = rpkeyFlow,
            active = post.reposted
        )
        PostAction(
            parameter = post.likeCount,
            iconNormal = Icons.Outlined.FavoriteBorder,
            iconActive = Icons.Default.Favorite,
            contentDescription = "Like",
            onClicked = onLikeClicked,
            onUnClicked = { onUnClicked(RecordType.Like, it) },
            rkey = lkeyFlow,
            active = post.liked
        )
        if (showMenu) {
            PostAction(
                parameter = -1,
                iconNormal = Icons.Default.MoreHoriz,
                contentDescription = "More",
                onClicked = {
                    menuExpanded = true
                },
                onUnClicked = {
                    menuExpanded = true
                },
            )
            PostMenu(menuExpanded, onMenuClicked, onDismissRequest = { menuExpanded = false })
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostAction(
    parameter: Long,
    iconNormal: ImageVector,
    modifier: Modifier = Modifier,
    iconActive: ImageVector = iconNormal,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String,
    onClicked: () -> Unit = { },
    onUnClicked: (rkey: String) -> Unit = {},
    active: Boolean = false,
    rkey: Flow<String?> = MutableStateFlow(""),
) {
    var clicked by rememberSaveable { mutableStateOf(active) }
    val inactiveColor = MaterialTheme.colorScheme.onSurface
    var num by rememberSaveable { mutableLongStateOf(parameter) }
    val color = remember { mutableStateOf(if (clicked) activeColor else inactiveColor) }
    val icon = remember { mutableStateOf(if (clicked) iconActive else iconNormal) }
    val key by rkey.collectAsStateWithLifecycle("")
    TextButton(
        onClick = {
            if (!clicked) {
                clicked = true
                color.value = activeColor
                icon.value = iconActive
                num++
                onClicked()
            } else {
                clicked = false
                color.value = inactiveColor
                icon.value = iconNormal
                num--
                onUnClicked(key.orEmpty())
            }
        },
        modifier = Modifier
            .padding(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon.value,
            contentDescription = contentDescription,
            tint = color.value,
            modifier = Modifier
                .size(20.dp)
                .padding(0.dp)
        )
        Text(
            text =  if (num > 0) num.toString() else "",
            color = color.value,
            modifier = Modifier.padding(horizontal = 6.dp)//.offset(y=(-1).dp)
        )
    }
}