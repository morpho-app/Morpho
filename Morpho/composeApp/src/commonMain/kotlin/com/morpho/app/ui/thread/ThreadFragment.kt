package com.morpho.app.ui.thread

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.atproto.repo.StrongRef
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.BskyPostThread
import com.morpho.app.model.bluesky.ThreadPost
import com.morpho.app.ui.common.OnPostClicked
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.post.BlockedPostFragment
import com.morpho.app.ui.post.FullPostFragment
import com.morpho.app.ui.post.NotFoundPostFragment
import com.morpho.app.ui.post.PostFragmentRole


@Composable
fun ThreadFragment(
    thread: BskyPostThread,
    modifier: Modifier = Modifier,
    comparator: Comparator<ThreadPost> = compareBy {
        if (it is ThreadPost.ViewablePost) {
            it.post.indexedAt.instant.epochSeconds
        } else {
            it.hashCode().toLong()
        }
    },
    onItemClicked: OnPostClicked = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val threadPost = remember { ThreadPost.ViewablePost(thread.post, thread.replies) }
    val hasReplies = rememberSaveable { threadPost.replies.isNotEmpty()}
    val rootIndex = remember { thread.parents.size }

    LaunchedEffect(Unit) {
        listState.scrollToItem(rootIndex)
    }
    val replies = remember {
        threadPost.replies.filterIsInstance<ThreadPost.ViewablePost>()
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        state = listState
    ) {
        if (thread.parents.isNotEmpty()) {
            when (val root = thread.parents[0]) {
                is ThreadPost.ViewablePost -> {
                    if (root.post.uri == thread.post.uri) {
                        item(key = threadPost.post.cid) {
                            FullPostFragment(
                                post = root.post,
                                onItemClicked = {onItemClicked(it) },
                                onProfileClicked = { onProfileClicked(it) },
                                onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                                onRepostClicked = { onRepostClicked(it) },
                                onReplyClicked = { onReplyClicked(it) },
                                onMenuClicked = { onMenuClicked(it) },
                                onLikeClicked = { onLikeClicked(it) },
                            )
                        }
                    } else {
                        itemsIndexed(thread.parents) { index, post ->
                            val reason = remember { when(post) {
                                is ThreadPost.BlockedPost -> null
                                is ThreadPost.NotFoundPost -> null
                                is ThreadPost.ViewablePost -> {
                                    post.post.reason
                                }
                            } }
                            val role = remember { when(index) {
                                0 -> PostFragmentRole.ThreadBranchStart
                                thread.parents.lastIndex -> PostFragmentRole.ThreadBranchEnd
                                else -> PostFragmentRole.ThreadBranchMiddle
                            } }
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
                        item {
                            ThreadItem(
                                item = threadPost,
                                role = PostFragmentRole.PrimaryThreadRoot,
                                reason = thread.post.reason,
                                onItemClicked = onItemClicked,
                                onProfileClicked = onProfileClicked,
                                onUnClicked = onUnClicked,
                                onRepostClicked = onRepostClicked,
                                onReplyClicked = onReplyClicked,
                                onMenuClicked = onMenuClicked,
                                onLikeClicked = onLikeClicked,
                            )
                        }
                    }
                }

                is ThreadPost.BlockedPost -> {
                    item {
                    BlockedPostFragment(
                        role = PostFragmentRole.Solo,
                        indentLevel = 0,
                    )
                    }
                }

                is ThreadPost.NotFoundPost -> {
                    item {
                    NotFoundPostFragment(
                        role = PostFragmentRole.Solo,
                        indentLevel = 0,
                    )
                    }
                }

            }
        } else {
            item {
                ThreadItem(
                    item = threadPost,
                    role = PostFragmentRole.PrimaryThreadRoot,
                    reason = thread.post.reason,
                    modifier = Modifier.padding(vertical = 4.dp),
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
        if (hasReplies){
            replies.fastForEach { reply ->
                    if (reply.replies.isNotEmpty()) {
                        item {
                            ThreadTree(
                                reply = reply, modifier = Modifier.padding(4.dp),
                                indentLevel = 1,
                                comparator = comparator,
                                onItemClicked = {onItemClicked(it) },
                                onProfileClicked = { onProfileClicked(it) },
                                onUnClicked =  { type,uri-> onUnClicked(type,uri) },
                                onRepostClicked = { onRepostClicked(it) },
                                onReplyClicked = { onReplyClicked(it) },
                                onMenuClicked = { onMenuClicked(it) },
                                onLikeClicked = { onLikeClicked(it) },
                            )
                        }
                    } else {
                        item {
                            ThreadItem(
                                item = reply, role = PostFragmentRole.Solo, indentLevel = 1,
                                modifier = Modifier.padding(4.dp),
                                onItemClicked = onItemClicked,
                                onProfileClicked = onProfileClicked,
                                onUnClicked = onUnClicked,
                                onRepostClicked = onRepostClicked,
                                onReplyClicked = onReplyClicked,
                                onMenuClicked = onMenuClicked,
                                onLikeClicked = onLikeClicked,
                            )
                        }
                    }

            }
        }
    }
}


