package radiant.nimbus.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atproto.repo.StrongRef
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.BskyPostThread
import radiant.nimbus.model.ThreadPost
import radiant.nimbus.ui.elements.MenuOptions
import radiant.nimbus.ui.post.PostFragment
import radiant.nimbus.ui.post.PostFragmentRole
import radiant.nimbus.ui.thread.ThreadItem
import radiant.nimbus.ui.thread.ThreadTree

@Composable
fun SkylineThreadFragment(
    thread: BskyPostThread,
    modifier: Modifier = Modifier,
    onItemClicked: OnPostClicked = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
) {
    val threadPost = remember { ThreadPost.ViewablePost(thread.post, thread.replies) }
    val hasReplies = rememberSaveable { threadPost.replies.isNotEmpty() }
    var showReplies by remember { mutableStateOf(threadPost.replies.size <= 2)}
    var showFullThread by remember { mutableStateOf(thread.parents.size <= 3)}

    Surface(
        tonalElevation = if (hasReplies) 1.dp else 0.dp,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = if (hasReplies) Modifier.padding(2.dp) else Modifier.fillMaxWidth()
    ) {
        Column(
        ) {
            if (thread.parents.isNotEmpty()) {
                when (val root = thread.parents[0]) {
                    is ThreadPost.ViewablePost -> if (root.post.uri == thread.post.uri) {
                        Surface(
                            tonalElevation = 1.dp,
                            //border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier
                                .padding(4.dp),
                        ) {
                            PostFragment(
                                post = root.post,
                                role = PostFragmentRole.ThreadBranchStart,
                                elevate = true,
                                modifier = Modifier,
                                onItemClicked = onItemClicked,
                                onProfileClicked = onProfileClicked,
                                onUnClicked = onUnClicked,
                                onRepostClicked = onRepostClicked,
                                onReplyClicked = onReplyClicked,
                                onMenuClicked = onMenuClicked,
                                onLikeClicked = onLikeClicked,
                            )
                        }
                    } else {
                        Surface(
                            tonalElevation = if (hasReplies) 1.dp else 0.dp,
                            //border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = if (hasReplies) Modifier.padding(2.dp) else Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(4.dp),
                            ) {
                                if(thread.parents.size > 3) {
                                    ThreadItem(
                                        item = thread.parents[0],
                                        role = PostFragmentRole.ThreadBranchStart,
                                        indentLevel = 1,
                                        elevate = true,
                                        onItemClicked = onItemClicked,
                                        onProfileClicked = onProfileClicked,
                                        onUnClicked = onUnClicked,
                                        onRepostClicked = onRepostClicked,
                                        onReplyClicked = onReplyClicked,
                                        onLikeClicked = onLikeClicked,
                                        onMenuClicked = onMenuClicked,
                                    )
                                    Surface(
                                        tonalElevation = 2.dp,
                                        shadowElevation = 1.dp,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                                    ) {
                                        TextButton(
                                            onClick = { showFullThread = !showFullThread },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = if (!showFullThread) {
                                                    Icons.AutoMirrored.Default.NavigateNext
                                                } else {
                                                    Icons.Default.ExpandMore
                                                },
                                                contentDescription = null)
                                            Spacer(modifier = Modifier
                                                .width(1.dp)
                                                .weight(0.3f))
                                            Text(
                                                text = if(!showFullThread) {
                                                    "Show full thread"
                                                } else {
                                                    "Hide thread"
                                                }
                                            )
                                        }
                                    }


                                    if (showFullThread) {
                                        thread.parents.forEachIndexed { index, post ->
                                            val reason = remember {
                                                when (post) {
                                                    is ThreadPost.BlockedPost -> null
                                                    is ThreadPost.NotFoundPost -> null
                                                    is ThreadPost.ViewablePost -> {
                                                        post.post.reason
                                                    }
                                                }
                                            }
                                            val role = remember {
                                                when (index) {
                                                    thread.parents.lastIndex -> PostFragmentRole.ThreadBranchEnd
                                                    0 -> PostFragmentRole.ThreadBranchStart
                                                    else -> PostFragmentRole.ThreadBranchMiddle
                                                }
                                            }
                                            if (post is ThreadPost.ViewablePost && (index < thread.parents.lastIndex) && (index != 0)) {
                                                ThreadItem(
                                                    item = post,
                                                    role = role,
                                                    indentLevel = 1,
                                                    reason = reason,
                                                    elevate = true,
                                                    onItemClicked = onItemClicked,
                                                    onProfileClicked = onProfileClicked,
                                                    onUnClicked = onUnClicked,
                                                    onRepostClicked = onRepostClicked,
                                                    onReplyClicked = onReplyClicked,
                                                    onLikeClicked = onLikeClicked,
                                                    onMenuClicked = onMenuClicked,
                                                )
                                            }
                                        }
                                    }
                                    ThreadItem(
                                        item = thread.parents[thread.parents.lastIndex],
                                        role = PostFragmentRole.ThreadBranchEnd,
                                        indentLevel = 1,
                                        elevate = true,
                                        onItemClicked = onItemClicked,
                                        onProfileClicked = onProfileClicked,
                                        onUnClicked = onUnClicked,
                                        onRepostClicked = onRepostClicked,
                                        onReplyClicked = onReplyClicked,
                                        onLikeClicked = onLikeClicked,
                                        onMenuClicked = onMenuClicked,
                                    )
                                } else {
                                    thread.parents.forEachIndexed { index, post ->
                                        val reason = remember {
                                            when (post) {
                                                is ThreadPost.BlockedPost -> null
                                                is ThreadPost.NotFoundPost -> null
                                                is ThreadPost.ViewablePost -> {
                                                    post.post.reason
                                                }
                                            }
                                        }
                                        val role = remember {
                                            when (index) {
                                                thread.parents.lastIndex -> PostFragmentRole.ThreadBranchEnd
                                                0 -> PostFragmentRole.ThreadBranchStart
                                                else -> PostFragmentRole.ThreadBranchMiddle
                                            }
                                        }
                                        if (post is ThreadPost.ViewablePost) {
                                            ThreadItem(
                                                item = post,
                                                role = role,
                                                indentLevel = 1,
                                                reason = reason,
                                                elevate = true,
                                                onItemClicked = onItemClicked,
                                                onProfileClicked = onProfileClicked,
                                                onUnClicked = onUnClicked,
                                                onRepostClicked = onRepostClicked,
                                                onReplyClicked = onReplyClicked,
                                                onLikeClicked = onLikeClicked,
                                                onMenuClicked = onMenuClicked,
                                            )
                                        }
                                    }
                                }

                                val role = remember {
                                    when (thread.parents.size) {
                                        0 -> PostFragmentRole.Solo
                                        1 -> PostFragmentRole.ThreadEnd
                                        else -> PostFragmentRole.Solo
                                    }
                                }
                                ThreadItem(
                                    item = threadPost,
                                    role = role,
                                    reason = thread.post.reason,
                                    elevate = true,
                                    modifier = Modifier
                                        .padding(4.dp),
                                    onItemClicked = onItemClicked,
                                    onProfileClicked = onProfileClicked,
                                    onUnClicked = onUnClicked,
                                    onRepostClicked = onRepostClicked,
                                    onReplyClicked = onReplyClicked,
                                    onLikeClicked = onLikeClicked,
                                    onMenuClicked = onMenuClicked,
                                )
                            }
                        }
                    }

                    is ThreadPost.BlockedPost -> {}
                    is ThreadPost.NotFoundPost -> {}
                }
            } else {
                val role = remember {
                    when (thread.parents.size) {
                        0 -> PostFragmentRole.Solo
                        1 -> PostFragmentRole.ThreadEnd
                        else -> PostFragmentRole.Solo
                    }
                }
                ThreadItem(
                    item = threadPost,
                    role = role,
                    reason = thread.post.reason,
                    elevate = true,
                    modifier = Modifier
                        .padding(4.dp),
                    onItemClicked = onItemClicked,
                    onProfileClicked = onProfileClicked,
                    onUnClicked = onUnClicked,
                    onRepostClicked = onRepostClicked,
                    onReplyClicked = onReplyClicked,
                    onLikeClicked = onLikeClicked,
                    onMenuClicked = onMenuClicked,
                )
            }

            if (hasReplies) {
                Surface(
                    tonalElevation = 1.dp,
                    //border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                    shape = MaterialTheme.shapes.extraSmall
                ) {

                    Column(
                        modifier = modifier
                            .padding(4.dp),
                    ) {
                        if(threadPost.replies.size > 2) {
                            TextButton(
                                onClick = { showReplies = !showReplies },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (!showReplies) {
                                        Icons.AutoMirrored.Default.NavigateNext
                                    } else {
                                        Icons.Default.ExpandMore
                                           },
                                    contentDescription = null)
                                Spacer(modifier = Modifier
                                    .width(1.dp)
                                    .weight(0.3f))
                                Text(
                                    text = if(!showReplies) {
                                        "Show replies"
                                    } else {
                                        "Hide replies"
                                    }
                                )
                            }
                        }
                        if (showReplies) {
                            threadPost.replies.forEach { it: ThreadPost ->
                                if (it is ThreadPost.ViewablePost) {
                                    if (it.replies.isNotEmpty()) {
                                        ThreadTree(
                                            reply = it, indentLevel = 1,
                                            modifier = Modifier.padding(4.dp),
                                            onItemClicked = onItemClicked,
                                            onProfileClicked = onProfileClicked,
                                            onUnClicked = onUnClicked,
                                            onRepostClicked = onRepostClicked,
                                            onReplyClicked = onReplyClicked,
                                            onLikeClicked = onLikeClicked,
                                            onMenuClicked = onMenuClicked,
                                        )
                                    } else {
                                        ThreadItem(
                                            item = it,
                                            role = PostFragmentRole.ThreadRootUnfocused,
                                            indentLevel = 1,
                                            modifier = Modifier.padding(4.dp),
                                            onItemClicked = onItemClicked,
                                            onProfileClicked = onProfileClicked,
                                            onUnClicked = onUnClicked,
                                            onRepostClicked = onRepostClicked,
                                            onReplyClicked = onReplyClicked,
                                            onLikeClicked = onLikeClicked,
                                            onMenuClicked = onMenuClicked,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

}