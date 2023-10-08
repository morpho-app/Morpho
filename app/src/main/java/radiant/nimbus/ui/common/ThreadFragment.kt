package radiant.nimbus.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyScopeMarker
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
) {val lineColour = MaterialTheme.colorScheme.secondary.copy(0.3f)
    /*Surface (
        tonalElevation = 0.dp,
        //shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, Brush.horizontalGradient(
            listOf(lineColour, Color.Transparent), endX = 40f
        )),
    ) {*/
        LazyColumn(
            modifier = modifier.heightIn(0.dp, 20000.dp),
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            state = listState
        ) {
            val threadPost = ThreadPost.ViewablePost(thread.post, thread.replies)
            if (thread.parents.isNotEmpty()) {
                when (val root = thread.parents[thread.parents.lastIndex]) {
                    is ThreadPost.ViewablePost -> {
                        if (root.post.uri == thread.post.uri) {
                            item {
                            FullPostFragment(
                                post = root.post,
                            )}
                        } else {
                            item {
                            PostFragment(
                                post = root.post,
                                role = PostFragmentRole.ThreadRootUnfocused,
                                indentLevel = 1,
                            )}
                            item {
                            ThreadItem(
                                item = threadPost,
                                role = PostFragmentRole.PrimaryThreadRoot,
                                reason = thread.post.reason,
                            )}
                        }
                    }

                    is ThreadPost.BlockedPost -> {
                        item {
                        BlockedPostFragment(
                            role = PostFragmentRole.Solo,
                            indentLevel = 0,
                        )}
                    }

                    is ThreadPost.NotFoundPost -> {
                        item {
                        NotFoundPostFragment(
                            role = PostFragmentRole.Solo,
                            indentLevel = 0,
                        )}
                    }

                }
            } else {
                item {
                    ThreadItem(
                        item = threadPost,
                        role = PostFragmentRole.PrimaryThreadRoot,
                        reason = thread.post.reason,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            threadPost.replies.forEach { it ->
                if(it is ThreadPost.ViewablePost) {
                    item { ThreadReply(item = it, role = PostFragmentRole.ThreadBranchEnd, indentLevel = 1, modifier = Modifier.padding(4.dp)) }
                    if (it.replies.isNotEmpty()) {
                        items(it.replies.sortedWith(comparator),
                            key = {
                                it.hashCode()
                            }
                        ) {reply ->
                            ThreadTree(reply = reply, indentLevel = 2, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }
    //}
}

@LazyScopeMarker
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
            ThreadReply(item = reply, role = PostFragmentRole.ThreadBranchEnd, indentLevel = indentLevel,modifier = Modifier.padding(vertical = 2.dp))
        } else {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)

            ) {
                val lineColour = if (indentLevel % 4 == 0) {
                    MaterialTheme.colorScheme.tertiary.copy(0.7f)
                } else if (indentLevel % 2 == 0){
                    MaterialTheme.colorScheme.secondary.copy(0.7f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(0.7f)
                }
            Surface (
                //shadowElevation = if (indentLevel > 0) 1.dp else 0.dp,
                border = BorderStroke(1.dp, Brush.sweepGradient(
                    0.0f to Color.Transparent, 0.2f to Color.Transparent,
                    0.4f to lineColour, 0.7f to lineColour,
                    0.9f to Color.Transparent,
                    center = Offset(100f, 500f)
                )),
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth(indentLevel(indentLevel / 2.0f))
                    .align(Alignment.End)


            ) {
                Column(
                ) {
                    ThreadReply(
                        item = reply,
                        role = PostFragmentRole.ThreadBranchStart,
                        indentLevel = indentLevel + 1,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    val nextIndent = indentLevel + 1
                    reply.replies.sortedWith(comparator).forEach {
                        ThreadTree(reply = it, indentLevel = nextIndent, modifier = modifier)
                    }
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
    elevate: Boolean = false,
    reason: BskyPostReason? = null
) {
    when(item) {
        is ThreadPost.ViewablePost -> {
            if (role == PostFragmentRole.PrimaryThreadRoot) {
                FullPostFragment(
                    post = item.post,
                )
            } else {
                PostFragment(
                    post = item.post,
                    role = role,
                    indentLevel = indentLevel,
                    elevate = elevate
                )
            }
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
                indentLevel = indentLevel,
                modifier = modifier,
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
