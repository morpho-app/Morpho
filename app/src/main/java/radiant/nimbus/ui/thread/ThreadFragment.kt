package radiant.nimbus.ui.thread

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atproto.repo.StrongRef
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.model.BskyPostThread
import radiant.nimbus.model.ThreadPost
import radiant.nimbus.ui.common.OnPostClicked
import radiant.nimbus.ui.elements.MenuOptions
import radiant.nimbus.ui.post.BlockedPostFragment
import radiant.nimbus.ui.post.FullPostFragment
import radiant.nimbus.ui.post.NotFoundPostFragment
import radiant.nimbus.ui.post.PostFragment
import radiant.nimbus.ui.post.PostFragmentRole


@Composable
fun ThreadFragment(
    thread: BskyPostThread,
    modifier: Modifier = Modifier,
    comparator: Comparator<ThreadPost> = compareBy {
        if (it is ThreadPost.ViewablePost) {
            it.post.indexedAt
        } else {
            it.hashCode()
        }
    },
    onItemClicked: OnPostClicked = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (StrongRef) -> Unit = { },
    onRepostClicked: (StrongRef) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    listState: LazyListState = rememberLazyListState()
) {
    val threadPost = remember { ThreadPost.ViewablePost(thread.post, thread.replies) }
    val hasReplies = rememberSaveable { threadPost.replies.isNotEmpty()}

    LazyColumn(
        modifier = modifier.heightIn(0.dp, 20000.dp).navigationBarsPadding(),
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
        state = listState
    ) {
        if (thread.parents.isNotEmpty()) {
            when (val root = thread.parents[thread.parents.lastIndex]) {
                is ThreadPost.ViewablePost -> {
                    if (root.post.uri == thread.post.uri) {
                        item {
                            FullPostFragment(
                                post = root.post,
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
                        item {
                        PostFragment(
                            post = root.post,
                            role = PostFragmentRole.ThreadRootUnfocused,
                            indentLevel = 1,
                            onItemClicked = onItemClicked,
                            onProfileClicked = onProfileClicked,
                            onUnClicked = onUnClicked,
                            onRepostClicked = onRepostClicked,
                            onReplyClicked = onReplyClicked,
                            onMenuClicked = onMenuClicked,
                            onLikeClicked = onLikeClicked,
                        )
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
                    onMenuClicked = onMenuClicked,
                    onLikeClicked = onLikeClicked,
                )
            }
        }
        if (hasReplies){
            threadPost.replies.forEach { it ->
                if(it is ThreadPost.ViewablePost) {
                    item {
                        ThreadReply(
                            item = it, role = PostFragmentRole.Solo, indentLevel = 1,
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
                    if (it.replies.isNotEmpty()) {
                        items(it.replies.sortedWith(comparator),
                            key = {
                                it.hashCode()
                            }
                        ) {reply ->
                            ThreadTree(
                                reply = reply, modifier = Modifier.padding(4.dp),
                                indentLevel = 2,
                                onItemClicked = onItemClicked,
                                onProfileClicked = onProfileClicked,
                                onReplyClicked = onReplyClicked,
                                onRepostClicked = onRepostClicked,
                                onLikeClicked = onLikeClicked,
                                onMenuClicked = onMenuClicked,
                                onUnClicked = onUnClicked,
                                )
                        }
                    }
                }
            }
        }
    }
}


