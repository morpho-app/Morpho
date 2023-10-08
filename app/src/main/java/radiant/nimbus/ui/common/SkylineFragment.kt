package radiant.nimbus.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.BskyPostThread
import radiant.nimbus.model.Skyline
import radiant.nimbus.model.SkylineItem
import radiant.nimbus.model.ThreadPost
import radiant.nimbus.ui.theme.NimbusTheme

typealias OnPostClicked = (BskyPost) -> Unit

class SkylineFragmentState(

)

@SuppressLint("UnrememberedMutableState")
@Composable
fun SkylineFragment (
    postFlow: StateFlow<Skyline>,
    onItemClicked: OnPostClicked,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    refresh: (String?) -> Unit = {},
) {
    val postList by postFlow.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(postList.posts.isNotEmpty() && !listState.canScrollForward) {
        refresh(postList.cursor)
    }
    val scrolledDownBy by  derivedStateOf { listState.firstVisibleItemIndex }
    Box(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = modifier,
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            state = listState
        ) {
            items(postList.posts) { skylineItem ->
                if( skylineItem.post != null) {
                    val post = skylineItem.post
                    val thread = skylineItem.thread
                    if (thread != null) {
                        SkylineThreadFragment(
                            thread = thread,
                            modifier = Modifier.fillParentMaxWidth().padding(vertical = 2.dp, horizontal = 4.dp),
                        )
                    } else if (post != null) {
                        PostFragment(
                            modifier = Modifier.fillParentMaxWidth().padding(vertical = 2.dp, horizontal = 4.dp),
                            post = post,
                            onItemClicked = onItemClicked,
                            elevate = true,
                        )
                    }
                }

            }
        }
        if(scrolledDownBy > 20) {
            OutlinedIconButton(onClick = {
                coroutineScope.launch {
                    val a = async { refresh(null) }
                    listState.scrollToItem(0)
                    a.await()
                } },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.8f)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(20.dp, 650.dp)
                    .size(50.dp)
            ) {
                Icon(
                    Icons.Default.KeyboardDoubleArrowUp,
                    "Scroll to top",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(5.dp)
                )
            }
        } else if(scrolledDownBy > 5) {
            OutlinedIconButton(onClick = {
                coroutineScope.launch {
                    val a = async { refresh(null) }
                    listState.animateScrollToItem(0)
                    a.await()
                } },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.8f)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(20.dp, 650.dp)
                    .size(50.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowUp,
                    "Scroll to top",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(5.dp)
                )
            }
        }
    }

}


@Composable
fun SkylineThreadFragment(
    thread: BskyPostThread,
    modifier: Modifier = Modifier,
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Column(
            modifier = modifier
                .padding(4.dp),
        ) {
            val threadPost = ThreadPost.ViewablePost(thread.post, thread.replies)
            if (thread.parents.isNotEmpty()) {
                when (val root = thread.parents[0]) {
                    is ThreadPost.ViewablePost -> if (root.post.uri == thread.post.uri) {
                        Surface(
                            tonalElevation = 1.dp,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            PostFragment(
                                post = root.post,
                                role = PostFragmentRole.ThreadRootUnfocused,
                                elevate = true,
                            )
                        }
                    } else {
                        Surface(
                            tonalElevation = 1.dp,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Column(
                                modifier = modifier
                                    .padding(2.dp),
                            ) {
                                thread.parents.forEachIndexed { index, post ->
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
                                        )
                                    }
                                }
                                ThreadItem(
                                    item = threadPost,
                                    role = PostFragmentRole.PrimaryThreadRoot,
                                    reason = thread.post.reason,
                                    elevate = true,
                                )
                            }
                        }
                    }
                    is ThreadPost.BlockedPost -> {}
                    is ThreadPost.NotFoundPost -> {}
                }
            }
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Column(
                    modifier = modifier
                        .padding(4.dp),
                ) {
                    threadPost.replies.forEach { it:ThreadPost ->
                        if (it is ThreadPost.ViewablePost) {
                            ThreadReply(item = it,
                                role = PostFragmentRole.ThreadBranchEnd,
                                indentLevel = 1,
                                modifier = Modifier.padding(4.dp)
                            )
                            if (it.replies.isNotEmpty()) {
                                it.replies.forEach { reply ->
                                    ThreadTree(reply = reply, indentLevel = 2, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
@Preview
fun PreviewSkyline() {
    NimbusTheme {
        ScreenBody(modifier = Modifier.height(1000.dp)) {
            val posts = mutableListOf<SkylineItem>()
            for (i in 1..10) {
                posts.add(SkylineItem(testPost))
            }
            val postsFlow = MutableStateFlow(Skyline(posts, null))
            SkylineFragment(postFlow = postsFlow.asStateFlow(), {})
        }
    }

}
