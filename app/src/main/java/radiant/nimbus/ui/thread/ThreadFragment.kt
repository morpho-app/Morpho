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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atproto.repo.StrongRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
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
        }},
    onItemClicked: OnPostClicked = {},
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onReplyClicked: (StrongRef) -> Unit = { },
    onRepostClicked: (StrongRef) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, rkey: String) -> Unit = { _, _ -> },
    lKeys: Flow<MutableMap<AtUri, String?>>,
    rpKeys: Flow<MutableMap<AtUri, String?>>,
    listState: LazyListState = rememberLazyListState()
) {
    val threadPost = remember { ThreadPost.ViewablePost(thread.post, thread.replies) }
    val hasReplies = rememberSaveable { threadPost.replies.isNotEmpty()}
    val likeKeys by lKeys.collectAsStateWithLifecycle(initialValue = mutableMapOf())
    val repostKeys by lKeys.collectAsStateWithLifecycle(initialValue = mutableMapOf())

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
                            var lkeyFlow: Flow<String?> = flowOf(null)
                            var rpkeyFlow: Flow<String?> = flowOf(null)
                            LaunchedEffect(likeKeys) {
                                lkeyFlow = snapshotFlow { likeKeys[root.post.uri] }
                                    .distinctUntilChanged()
                            }
                            LaunchedEffect(repostKeys) {
                                rpkeyFlow = snapshotFlow { likeKeys[root.post.uri] }
                                    .distinctUntilChanged()
                            }
                            FullPostFragment(
                                post = root.post,
                                onItemClicked = onItemClicked,
                                onProfileClicked = onProfileClicked,
                                onUnClicked = onUnClicked,
                                onRepostClicked = onRepostClicked,
                                onReplyClicked = onReplyClicked,
                                onMenuClicked = onMenuClicked,
                                onLikeClicked = onLikeClicked,
                                lkeyFlow = lkeyFlow,
                                rpkeyFlow = rpkeyFlow,
                            )
                        }
                    } else {
                        item {
                            var lkeyFlow: Flow<String?> = flowOf(null)
                            var rpkeyFlow: Flow<String?> = flowOf(null)
                            LaunchedEffect(likeKeys) {
                                lkeyFlow = snapshotFlow { likeKeys[root.post.uri] }
                                    .distinctUntilChanged()
                            }
                            LaunchedEffect(repostKeys) {
                                rpkeyFlow = snapshotFlow { likeKeys[root.post.uri] }
                                    .distinctUntilChanged()
                            }
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
                            lkeyFlow = lkeyFlow,
                            rpkeyFlow = rpkeyFlow,
                        )
                        }
                        item {
                            var lkeyFlow: Flow<String?> = flowOf(null)
                            var rpkeyFlow: Flow<String?> = flowOf(null)
                            LaunchedEffect(likeKeys) {
                                lkeyFlow = snapshotFlow { likeKeys[threadPost.post.uri] }
                                    .distinctUntilChanged()
                            }
                            LaunchedEffect(repostKeys) {
                                rpkeyFlow = snapshotFlow { likeKeys[threadPost.post.uri] }
                                    .distinctUntilChanged()
                            }
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
                                lkeyFlow = lkeyFlow,
                                rpkeyFlow = rpkeyFlow,
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
                var lkeyFlow: Flow<String?> = flowOf(null)
                var rpkeyFlow: Flow<String?> = flowOf(null)
                LaunchedEffect(likeKeys) {
                    lkeyFlow = snapshotFlow { likeKeys[threadPost.post.uri] }
                        .distinctUntilChanged()
                }
                LaunchedEffect(repostKeys) {
                    rpkeyFlow = snapshotFlow { likeKeys[threadPost.post.uri] }
                        .distinctUntilChanged()
                }
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
                    lkeyFlow = lkeyFlow,
                    rpkeyFlow = rpkeyFlow,
                )
            }
        }
        if (hasReplies){
            threadPost.replies.forEach { it ->
                if(it is ThreadPost.ViewablePost) {
                    item {
                        var lkeyFlow: Flow<String?> = flowOf(null)
                        var rpkeyFlow: Flow<String?> = flowOf(null)
                        LaunchedEffect(likeKeys) {
                            lkeyFlow = snapshotFlow { likeKeys[it.post.uri] }
                                .distinctUntilChanged()
                        }
                        LaunchedEffect(repostKeys) {
                            rpkeyFlow = snapshotFlow { likeKeys[it.post.uri] }
                                .distinctUntilChanged()
                        }
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
                            lkeyFlow = lkeyFlow,
                            rpkeyFlow = rpkeyFlow,
                        )
                    }
                    if (it.replies.isNotEmpty()) {
                        items(it.replies.sortedWith(comparator),
                            key = {
                                it.hashCode()
                            }
                        ) {reply ->
                            ThreadTree(
                                reply = reply, indentLevel = 2,
                                modifier = Modifier.padding(4.dp),
                                onItemClicked = onItemClicked,
                                onProfileClicked = onProfileClicked,
                                onUnClicked = onUnClicked,
                                onRepostClicked = onRepostClicked,
                                onReplyClicked = onReplyClicked,
                                onMenuClicked = onMenuClicked,
                                onLikeClicked = onLikeClicked,
                                lKeys = lKeys,
                                rpKeys = rpKeys,
                                )
                        }
                    }
                }
            }
        }
    }
}


