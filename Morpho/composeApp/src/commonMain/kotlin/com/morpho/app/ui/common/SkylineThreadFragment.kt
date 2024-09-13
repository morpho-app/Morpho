package com.morpho.app.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.BskyPostThread
import com.morpho.app.model.bluesky.ThreadPost
import com.morpho.app.model.uidata.ContentHandling
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.post.PostFragment
import com.morpho.app.ui.post.PostFragmentRole
import com.morpho.app.ui.thread.ThreadItem
import com.morpho.app.ui.thread.ThreadTree
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType

@Composable
inline fun SkylineThreadFragment(
    thread: BskyPostThread,
    modifier: Modifier = Modifier,
    crossinline onItemClicked: OnPostClicked = {},
    crossinline onProfileClicked: (AtIdentifier) -> Unit = {},
    crossinline onReplyClicked: (BskyPost) -> Unit = { },
    crossinline onRepostClicked: (BskyPost) -> Unit = { },
    crossinline onLikeClicked: (StrongRef) -> Unit = { },
    noinline onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
    crossinline onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    crossinline getContentHandling: (BskyPost) -> List<ContentHandling> = { listOf() },
    debuggable: Boolean = false,
) {
    val threadPost = remember { ThreadPost.ViewablePost(thread.post, thread.replies) }
    val hasReplies = rememberSaveable { threadPost.replies.isNotEmpty() }
    var showReplies by remember { mutableStateOf(threadPost.replies.size <= 2)}
    var showFullThread by remember { mutableStateOf(thread.parents.size <= 3)}
    val parents = remember { thread.parents.distinctBy { it.uri } }

    Surface(
        tonalElevation = if (hasReplies) 1.dp else 0.dp,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = if (hasReplies) modifier.padding(2.dp) else modifier.fillMaxWidth()
    ) {
        Column {
            if (parents.isNotEmpty()) {
                when (val root = parents[0]) {
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
                                role = PostFragmentRole.Solo,
                                elevate = true,
                                modifier = if(debuggable) Modifier.border(1.dp, Color.Cyan) else Modifier,
                                onItemClicked = {onItemClicked(it) },
                                onProfileClicked = { onProfileClicked(it) },
                                onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                                onRepostClicked = { onRepostClicked(it) },
                                onReplyClicked = { onReplyClicked(it) },
                                onMenuClicked = onMenuClicked,
                                onLikeClicked = { onLikeClicked(it) },
                                getContentHandling = { getContentHandling(it) }
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
                                if(parents.size > 3) {
                                    ThreadItem(
                                        item = thread.parents[0],
                                        modifier = if(debuggable) Modifier.border(1.dp, Color.Green) else Modifier,
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
                                        getContentHandling = getContentHandling
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
                                        parents.fastForEachIndexed { index, post ->
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
                                                    0 -> PostFragmentRole.Solo
                                                    1 -> PostFragmentRole.ThreadBranchStart
                                                    parents.lastIndex -> PostFragmentRole.ThreadBranchEnd
                                                    else -> PostFragmentRole.ThreadBranchMiddle
                                                }
                                            }
                                            if (
                                                post is ThreadPost.ViewablePost
                                                && post.uri != threadPost.uri
                                                && (index > 0 || parents.lastIndex < 2)
                                                && index < parents.lastIndex
                                            ) {
                                                ThreadItem(
                                                    item = post,
                                                    role = role,
                                                    modifier = if(debuggable) Modifier.border(1.dp, Color.White) else Modifier,
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
                                                    getContentHandling = getContentHandling
                                                )
                                            }
                                        }
                                    }
                                    if (parents[parents.lastIndex] is ThreadPost.ViewablePost) {
                                        ThreadItem(
                                            item = parents[parents.lastIndex],
                                            role = PostFragmentRole.ThreadBranchEnd,
                                            indentLevel = 1,
                                            modifier = if (debuggable) Modifier.border(
                                                1.dp,
                                                Color.Yellow
                                            ) else Modifier,
                                            elevate = true,
                                            onItemClicked = onItemClicked,
                                            onProfileClicked = onProfileClicked,
                                            onUnClicked = onUnClicked,
                                            onRepostClicked = onRepostClicked,
                                            onReplyClicked = onReplyClicked,
                                            onLikeClicked = onLikeClicked,
                                            onMenuClicked = onMenuClicked,
                                            getContentHandling = getContentHandling
                                        )
                                    }
                                } else {
                                    parents.fastForEachIndexed { index, post ->
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
                                                0 -> PostFragmentRole.ThreadRootUnfocused
                                                parents.lastIndex -> PostFragmentRole.ThreadBranchEnd
                                                else -> PostFragmentRole.ThreadBranchMiddle
                                            }
                                        }
                                        if (post is ThreadPost.ViewablePost
                                            && post.uri != threadPost.uri
                                        ) {
                                            ThreadItem(
                                                item = post,
                                                role = role,
                                                modifier = if(debuggable) Modifier.border(1.dp, Color.Red) else Modifier,
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
                                                getContentHandling = getContentHandling
                                            )
                                        }
                                    }
                                }

                                val role = remember {
                                    when (parents.size) {
                                        0 -> PostFragmentRole.Solo
                                        1 -> PostFragmentRole.Solo
                                        else -> PostFragmentRole.Solo
                                    }
                                }
                                ThreadItem(
                                    item = threadPost,
                                    role = role,
                                    reason = null,
                                    elevate = true,
                                    modifier = if(debuggable) Modifier.border(1.dp, Color.Magenta) else Modifier,
                                        //.padding(4.dp),
                                    onItemClicked = onItemClicked,
                                    onProfileClicked = onProfileClicked,
                                    onUnClicked = onUnClicked,
                                    onRepostClicked = onRepostClicked,
                                    onReplyClicked = onReplyClicked,
                                    onLikeClicked = onLikeClicked,
                                    onMenuClicked = onMenuClicked,
                                    getContentHandling = getContentHandling
                                )
                            }
                        }
                    }

                    is ThreadPost.BlockedPost -> {}
                    is ThreadPost.NotFoundPost -> {}
                }
            } else {
                val role = remember {
                    when (parents.size) {
                        0 -> PostFragmentRole.Solo
                        1 -> PostFragmentRole.ThreadEnd
                        else -> PostFragmentRole.Solo
                    }
                }
                ThreadItem(
                    item = threadPost,
                    role = role,
                    reason = null,
                    elevate = true,
                    modifier = if(debuggable) Modifier.border(1.dp, Color.Blue) else Modifier
                        .padding(4.dp),
                    onItemClicked = onItemClicked,
                    onProfileClicked = onProfileClicked,
                    onUnClicked = onUnClicked,
                    onRepostClicked = onRepostClicked,
                    onReplyClicked = onReplyClicked,
                    onLikeClicked = onLikeClicked,
                    onMenuClicked = onMenuClicked,
                    getContentHandling = getContentHandling
                )
            }

            if (hasReplies) {
                Surface(
                    modifier = if(debuggable) Modifier.border(1.dp, Color.Black) else Modifier,
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
                            val replies = remember {threadPost.replies.filterIsInstance<ThreadPost.ViewablePost>()}
                            replies.fastForEach { post: ThreadPost ->
                                if (post is ThreadPost.ViewablePost) {
                                    if (post.replies.isNotEmpty() && replies.size > 1) {
                                        ThreadTree(
                                            reply = post, indentLevel = 1,
                                            modifier = Modifier.padding(4.dp),
                                            onItemClicked = {onItemClicked(it) },
                                            onProfileClicked = { onProfileClicked(it) },
                                            onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                                            onRepostClicked = { onRepostClicked(it) },
                                            onReplyClicked = { onReplyClicked(it) },
                                            onMenuClicked = { menu, p -> onMenuClicked(menu, p) },
                                            onLikeClicked = { onLikeClicked(it) },
                                            getContentHandling = { getContentHandling(it) }
                                        )
                                    } else {
                                        ThreadItem(
                                            item = post,
                                            role = PostFragmentRole.ThreadEnd,
                                            indentLevel = 1,
                                            modifier = Modifier.padding(4.dp),
                                            onItemClicked = onItemClicked,
                                            onProfileClicked = onProfileClicked,
                                            onUnClicked = onUnClicked,
                                            onRepostClicked = onRepostClicked,
                                            onReplyClicked = onReplyClicked,
                                            onLikeClicked = onLikeClicked,
                                            onMenuClicked = onMenuClicked,
                                            getContentHandling = getContentHandling
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