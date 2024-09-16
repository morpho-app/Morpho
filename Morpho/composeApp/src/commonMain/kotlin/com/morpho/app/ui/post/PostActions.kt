package com.morpho.app.ui.post

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.elements.PostMenu
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType

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
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PostAction(
            parameter = post.replyCount,
            iconNormal = Icons.Outlined.ChatBubbleOutline,
            contentDescription = "Reply ",
            onClicked = {
                onReplyClicked()

            },
            onUnClicked = {  },
        )
        PostAction(
            parameter = post.repostCount,
            iconNormal = Icons.Outlined.Repeat,
            contentDescription = "Repost ",
            onClicked = onRepostClicked,
            onUnClicked = { onUnClicked(RecordType.Repost, post.repostUri ?: post.uri) },
            active = post.reposted
        )
        PostAction(
            parameter = post.likeCount,
            iconNormal = Icons.Outlined.FavoriteBorder,
            iconActive = Icons.Default.Favorite,
            contentDescription = "Like ",
            activeColor = Color(0xFFEC7B9E),
            onClicked = onLikeClicked,
            onUnClicked = { onUnClicked(RecordType.Like, post.likeUri ?: post.uri) },
            active = post.liked
        )
        if (showMenu) {

            PostAction(
                parameter = -1,
                iconNormal = Icons.Default.MoreHoriz,
                contentDescription = "More ",
                onClicked = {
                    menuExpanded = true
                },
                onUnClicked = {
                    menuExpanded = true
                },
            )
            DisableSelection { PostMenu(menuExpanded, onMenuClicked, onDismissRequest = { menuExpanded = false }) }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DummyPostActions(
    modifier: Modifier = Modifier,
    showMenu: Boolean = true,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PostAction(
            parameter = 0,
            iconNormal = Icons.Outlined.ChatBubbleOutline,
            contentDescription = "Reply ",

            onUnClicked = {  },
        )
        PostAction(
            parameter = 0,
            iconNormal = Icons.Outlined.Repeat,
            contentDescription = "Repost ",
            active = false
        )
        PostAction(
            parameter = 0,
            iconNormal = Icons.Outlined.FavoriteBorder,
            iconActive = Icons.Default.Favorite,
            contentDescription = "Like ",
            activeColor = Color(0xFFEC7B9E),
            active = false
        )
        if (showMenu) {

            PostAction(
                parameter = -1,
                iconNormal = Icons.Default.MoreHoriz,
                contentDescription = "More ",
                onClicked = {
                    menuExpanded = true
                },
                onUnClicked = {
                    menuExpanded = true
                },
            )
        }

    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
inline fun PostAction(
    parameter: Long,
    iconNormal: ImageVector,
    modifier: Modifier = Modifier,
    iconActive: ImageVector = iconNormal,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String,
    crossinline onClicked: () -> Unit = { },
    crossinline onUnClicked: () -> Unit = {},
    active: Boolean = false,
) {
    var clicked by rememberSaveable { mutableStateOf(active) }
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    var num by rememberSaveable { mutableLongStateOf(parameter) }
    val color = remember { mutableStateOf(if (clicked) activeColor else inactiveColor) }
    val icon = remember { mutableStateOf(if (clicked) iconActive else iconNormal) }
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
                onUnClicked()
            }
        },
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
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
            text =  if (num > 0) "$num" else "",
            color = color.value,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}