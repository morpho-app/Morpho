package radiant.nimbus.ui.common

import ThreadViewPostUnion
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import app.bsky.feed.ThreadViewPostReplieUnion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.model.BskyPostReason
import radiant.nimbus.model.BskyPostThread
import radiant.nimbus.model.toPost

typealias ThreadLoadFunction = (ApiProvider) -> Unit

class ThreadFragmentState(
    _thread: BskyPostThread,
    val apiProvider: ApiProvider
) {
    var thread by mutableStateOf(_thread)
        private set

    fun loadThread(scope: CoroutineScope) = scope.launch(Dispatchers.IO) {
        thread.getThread(apiProvider)
    }
    val isLoaded: Boolean
        get() = thread.root != null
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
) {
    ConstraintLayout {
        Column {
            when(val root = thread.root) {
                is ThreadViewPostUnion.ThreadViewPost -> {
                    if (root.value == thread.cursor) {
                        PostFragment(
                            post = root.value.post.toPost(reply = thread.entry.reply, reason = thread.entry.reason),
                            role = PostFragmentRole.PrimaryThreadRoot,
                            indentLevel = 0,
                            )
                    } else {
                        PostFragment(
                            post = root.value.post.toPost(),
                            role = PostFragmentRole.ThreadRootUnfocused,
                            indentLevel = 1,
                            )
                    }
                }
                is ThreadViewPostUnion.BlockedPost -> {
                    BlockedPostFragment(
                        post = root.value,
                        role = PostFragmentRole.Solo,
                        indentLevel = 0,
                        )
                }
                is ThreadViewPostUnion.NotFoundPost -> {
                    NotFoundPostFragment(
                        post = root.value,
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
            thread.cursor?.let { ThreadViewPostUnion.ThreadViewPost(it) }?.let {
                ThreadItem(
                    item = it,
                    role = PostFragmentRole.PrimaryThreadRoot,
                    reason = thread.entry.reason,
                )
            }
            thread.cursor?.replies?.forEach {
                if(it is ThreadViewPostReplieUnion.ThreadViewPost) {
                    if (it.value.replies.isEmpty()) {
                        ThreadReply(item = it, role = PostFragmentRole.ThreadBranchEnd, indentLevel = 1)
                    } else {
                        ThreadReply(item = it, role = PostFragmentRole.ThreadBranchStart, indentLevel = 1)
                        it.value.replies.forEach {
                            ThreadTree(reply = it, indentLevel = 2)
                        }
                    }
                }
            }
        }
    }


}

@Composable
fun ThreadTree(reply: ThreadViewPostReplieUnion, indentLevel: Int = 1) {
    if(reply is ThreadViewPostReplieUnion.ThreadViewPost) {
        if (reply.value.replies.isEmpty()) {
            ThreadReply(item = reply, role = PostFragmentRole.ThreadBranchEnd, indentLevel = indentLevel)
        } else {
            ThreadReply(item = reply, role = PostFragmentRole.ThreadBranchStart, indentLevel = indentLevel)
            reply.value.replies.forEach {
                val nextIndent = indentLevel + 1
                ThreadTree(reply = reply, indentLevel = nextIndent)
            }
        }
    }
}

@Composable
fun ThreadItem(
    item: ThreadViewPostUnion,
    modifier: Modifier = Modifier,
    indentLevel: Int = 0,
    role: PostFragmentRole = PostFragmentRole.ThreadBranchStart,
    reason: BskyPostReason? = null
) {
    when(item) {
        is ThreadViewPostUnion.ThreadViewPost -> {
            PostFragment(
                post = item.value.post.toPost(null, reason),
                role = role,
                indentLevel = indentLevel,
                )
        }
        is ThreadViewPostUnion.BlockedPost -> {
            BlockedPostFragment(
                post = item.value,
                role = role,
                indentLevel = indentLevel,
            )
        }
        is ThreadViewPostUnion.NotFoundPost -> {
            NotFoundPostFragment(
                post = item.value,
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
    item: ThreadViewPostReplieUnion,
    modifier: Modifier = Modifier,
    indentLevel: Int = 1,
    role: PostFragmentRole = PostFragmentRole.ThreadBranchMiddle,
) {
    when(item) {
        is ThreadViewPostReplieUnion.ThreadViewPost -> {
            val r = if (item.value.replies.isEmpty()) {
                PostFragmentRole.ThreadBranchEnd
            } else {
                PostFragmentRole.ThreadBranchMiddle
            }
            PostFragment(
                post = item.value.post.toPost(),
                role = r)
        }
        is ThreadViewPostReplieUnion.BlockedPost -> {
            BlockedPostFragment(post = item.value)
        }
        is ThreadViewPostReplieUnion.NotFoundPost -> {
            NotFoundPostFragment(post = item.value)
        }
        else -> {
            NotFoundPostFragment()
        }
    }
}