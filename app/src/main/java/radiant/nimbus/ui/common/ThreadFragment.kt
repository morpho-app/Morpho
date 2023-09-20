package radiant.nimbus.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.model.BskyPostReason
import radiant.nimbus.model.BskyPostThread
import radiant.nimbus.model.ThreadPost


class ThreadFragmentState(
    _thread: BskyPostThread,
    val apiProvider: ApiProvider
) {
    var thread by mutableStateOf(_thread)
        private set

    fun loadThread(scope: CoroutineScope) = scope.launch(Dispatchers.IO) {

    }
    val isLoaded: Boolean
        get() = thread.post != null
}

@Composable
fun ThreadFragment(
    _threadState: ThreadFragmentState,
    modifier: Modifier = Modifier,
) {
    val threadState by rememberSaveable { mutableStateOf(_threadState) }
    val isLoaded by rememberSaveable { mutableStateOf(threadState.isLoaded) }
    when {
        isLoaded -> {
            ThreadFragmentFrame(threadState.thread, modifier)
        }
        else -> LaunchedEffect(Unit) {
            threadState.loadThread(this)
        }
    }

}

@Composable
fun ThreadFragmentFrame(
    thread: BskyPostThread,
    modifier: Modifier = Modifier,
    comparator: Comparator<ThreadPost> = compareBy {
        if (it is ThreadPost.ViewablePost) {
            it.post.indexedAt
        } else {
            it.hashCode()
        }},
    listState: LazyListState = rememberLazyListState()
) {
    ConstraintLayout {
        LazyColumn(
            modifier = modifier,
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            state = listState
        ) {
            items(thread.parents.size + thread.replies.size + 1) {
                if(thread.parents.isNotEmpty()) {
                    when(val root = thread.parents[0] ) {
                        is ThreadPost.ViewablePost -> {
                            if (root.post.uri == thread.post.uri) {
                                PostFragment(
                                    post = root.post,
                                    role = PostFragmentRole.PrimaryThreadRoot,
                                    indentLevel = 0,
                                )
                            } else {
                                PostFragment(
                                    post = root.post,
                                    role = PostFragmentRole.ThreadRootUnfocused,
                                    indentLevel = 1,
                                )
                            }
                        }
                        is ThreadPost.BlockedPost -> {
                            BlockedPostFragment(
                                role = PostFragmentRole.Solo,
                                indentLevel = 0,
                            )
                        }
                        is ThreadPost.NotFoundPost -> {
                            NotFoundPostFragment(
                                role = PostFragmentRole.Solo,
                                indentLevel = 0,
                            )
                        }
                        else -> {
                            NotFoundPostFragment(
                                role = PostFragmentRole.Solo,
                                indentLevel = 0,
                            )
                        }
                    }
                }
                val threadPost = ThreadPost.ViewablePost(thread.post, thread.replies)
                ThreadItem(
                    item = threadPost,
                    role = PostFragmentRole.PrimaryThreadRoot,
                    reason = thread.post.reason,
                )

                threadPost.replies.forEach {
                    if(it is ThreadPost.ViewablePost) {
                        if (it.replies.isEmpty()) {
                            ThreadReply(item = it, role = PostFragmentRole.ThreadBranchEnd, indentLevel = 1)
                        } else {
                            ThreadReply(item = it, role = PostFragmentRole.ThreadBranchStart, indentLevel = 1)
                            it.replies.sortedWith(comparator).forEach {
                                ThreadTree(reply = it,
                                    indentLevel = 2,
                                    //comparator = comparator,
                                )
                            }
                        }
                    }
                }
            }

        }
    }


}

@Composable
fun ThreadTree(
    reply: ThreadPost,
    modifier: Modifier = Modifier,
    indentLevel: Int = 1,
    comparator: Comparator<ThreadPost> = compareBy {
        if (it is ThreadPost.ViewablePost) {
            it.post.indexedAt
        } else {
            it.hashCode()
        }},
) {
    if(reply is ThreadPost.ViewablePost) {
        if (reply.replies.isEmpty()) {
            ThreadReply(item = reply, role = PostFragmentRole.ThreadBranchEnd, indentLevel = indentLevel)
        } else {
            Surface (
                tonalElevation = indentLevel.dp
            ){
                Column(
                    Modifier.padding(vertical = 2.dp)
                ) {
                    ThreadReply(item = reply, role = PostFragmentRole.ThreadBranchStart, indentLevel = indentLevel)
                    reply.replies.sortedWith(comparator).forEach {
                        val nextIndent = indentLevel + 1
                        ThreadTree(reply = it, indentLevel = nextIndent)
                    }
                }
            }

        }
    }
}

@Composable
fun ThreadItem(
    item: ThreadPost,
    modifier: Modifier = Modifier,
    indentLevel: Int = 0,
    role: PostFragmentRole = PostFragmentRole.ThreadBranchStart,
    reason: BskyPostReason? = null
) {
    when(item) {
        is ThreadPost.ViewablePost -> {
            PostFragment(
                post = item.post,
                role = role,
                indentLevel = indentLevel,
                )
        }
        is ThreadPost.BlockedPost -> {
            BlockedPostFragment(
                //post = item.value,
                role = role,
                indentLevel = indentLevel,
            )
        }
        is ThreadPost.NotFoundPost -> {
            NotFoundPostFragment(
                //post = item.value,
                role = role,
                indentLevel = indentLevel,
            )
        }
        else -> {
            NotFoundPostFragment(
                role = role,
                indentLevel = indentLevel,
            )
        }
    }
}

@Composable
fun ThreadReply(
    item: ThreadPost,
    modifier: Modifier = Modifier,
    indentLevel: Int = 1,
    role: PostFragmentRole = PostFragmentRole.ThreadBranchMiddle,
) {
    when(item) {
        is ThreadPost.ViewablePost -> {
            val r = if (item.replies.isEmpty()) {
                PostFragmentRole.ThreadBranchEnd
            } else {
                PostFragmentRole.ThreadBranchMiddle
            }
            PostFragment(
                post = item.post,
                role = r,
                indentLevel = indentLevel
            )
        }
        is ThreadPost.BlockedPost -> {
            BlockedPostFragment()
        }
        is ThreadPost.NotFoundPost -> {
            NotFoundPostFragment()
        }
        else -> {
            NotFoundPostFragment()
        }
    }
}
