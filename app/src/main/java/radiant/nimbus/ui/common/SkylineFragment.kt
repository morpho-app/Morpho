package radiant.nimbus.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atproto.repo.StrongRef
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordType
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.BskyPostThread
import radiant.nimbus.model.Skyline
import radiant.nimbus.model.SkylineItem
import radiant.nimbus.model.ThreadPost
import radiant.nimbus.ui.elements.MenuOptions
import radiant.nimbus.ui.post.PostFragment
import radiant.nimbus.ui.post.PostFragmentRole
import radiant.nimbus.ui.post.testPost
import radiant.nimbus.ui.theme.NimbusTheme
import radiant.nimbus.ui.thread.ThreadItem
import radiant.nimbus.ui.thread.ThreadReply
import radiant.nimbus.ui.thread.ThreadTree

typealias OnPostClicked = (AtUri) -> Unit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkylineFragment (
    navigator: DestinationsNavigator,
    postFlow: StateFlow<Skyline>,
    modifier: Modifier = Modifier,
    onItemClicked: OnPostClicked,
    onProfileClicked: (AtIdentifier) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    onPostButtonClicked: () -> Unit = {},
    refresh: (String?) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isProfileFeed: Boolean = false,
) {
    val postList by postFlow.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        refresh(postList.cursor)
        if(listState.firstVisibleItemIndex == 0 && !isProfileFeed) listState.animateScrollToItem(0, 50)
    }
    LaunchedEffect(postList.posts.size > 10 && !listState.canScrollForward) {
        refresh(postList.cursor)
    }
    val scrolledDownBy by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    ConstraintLayout(
        modifier = if(isProfileFeed) {
            Modifier.fillMaxWidth().systemBarsPadding()
        }   else {
            Modifier
            .fillMaxSize()
            .systemBarsPadding()
        },
    ) {
        val (scrollButton, postButton, skyline) = createRefs()
        val leftGuideline = createGuidelineFromStart(40.dp)
        val rightGuideline = createGuidelineFromEnd(40.dp)
        val buttonGuideline = createGuidelineFromBottom(100.dp)
        LazyColumn(
            modifier = modifier.constrainAs(skyline) {
                 top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            contentPadding = if(isProfileFeed) {
                contentPadding
            }   else {
                PaddingValues(
                    bottom = contentPadding.calculateBottomPadding(),
                    top = WindowInsets.safeContent.only(WindowInsetsSides.Top).asPaddingValues()
                        .calculateTopPadding()
                )
            },
            verticalArrangement = Arrangement.Top,
            state = listState
        ) {
            if(!isProfileFeed) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Spacer(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .weight(0.4f)
                        )
                        IconButton(
                            onClick = { refresh(null) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .weight(0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh, contentDescription = "Refresh",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        TextButton(
                            onClick = { /*TODO*/ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                            //.weight(0.5f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings, contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                            )
                            Text(text = "Feed settings", Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }
            items(postList.posts) { skylineItem ->
                if (skylineItem.post != null) {
                    val post = skylineItem.post
                    val thread = skylineItem.thread
                    if (thread != null) {
                        SkylineThreadFragment(
                            thread = thread,
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            onItemClicked = onItemClicked,
                            onProfileClicked = onProfileClicked,
                            onUnClicked = onUnClicked,
                            onRepostClicked = onRepostClicked,
                            onReplyClicked = onReplyClicked,
                            onMenuClicked = onMenuClicked,
                            onLikeClicked = onLikeClicked,
                        )
                    } else if (post != null) {
                        PostFragment(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            post = post,
                            onItemClicked = onItemClicked,
                            onProfileClicked = onProfileClicked,
                            elevate = true,
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
        // TODO: Rework the layout to go from the bottom, using constraints
        if (scrolledDownBy > 5) {
            OutlinedIconButton(
                onClick = {
                    coroutineScope.launch {
                        val a = async { refresh(null) }
                        listState.animateScrollToItem(0)
                        a.await()
                    }
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        1.dp
                    ).copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .size(50.dp)
                    .constrainAs(scrollButton) {
                        centerAround(buttonGuideline)
                        centerAround(leftGuideline)
                    }
            ) {
                if (scrolledDownBy > 20) {
                    Icon(
                        Icons.Default.KeyboardDoubleArrowUp,
                        "Scroll to top",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(5.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        "Scroll to top",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(5.dp)
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { onPostButtonClicked() },
            modifier = Modifier
                .constrainAs(postButton) {
                    centerAround(buttonGuideline)
                    centerAround(rightGuideline)
                }
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = "Post a thing!",

                )
        }
    }

}


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
    val hasReplies = rememberSaveable { threadPost.replies.isNotEmpty()}

    Surface(
        tonalElevation = if(hasReplies) 1.dp else 0.dp,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = if(hasReplies) Modifier.padding(2.dp) else Modifier.fillMaxWidth()
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
                            tonalElevation = if(hasReplies) 1.dp else 0.dp,
                            //border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = if(hasReplies) Modifier.padding(2.dp) else Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(4.dp),
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
                                        thread.parents.lastIndex -> PostFragmentRole.ThreadBranchStart
                                        0 -> PostFragmentRole.ThreadBranchEnd
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
                                val role = remember { when(thread.parents.size) {
                                    0 -> PostFragmentRole.Solo
                                    1 -> PostFragmentRole.ThreadEnd
                                    else -> PostFragmentRole.Solo
                                } }
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
                val role = remember { when(thread.parents.size) {
                    0 -> PostFragmentRole.Solo
                    1 -> PostFragmentRole.ThreadEnd
                    else -> PostFragmentRole.Solo
                } }
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
                        threadPost.replies.forEach { it:ThreadPost ->
                            if (it is ThreadPost.ViewablePost) {
                                val threadHasReplies = rememberSaveable {
                                    it.replies.isNotEmpty()
                                }
                                ThreadReply(
                                    item = it,
                                    role = if(threadHasReplies) PostFragmentRole.ThreadBranchStart else PostFragmentRole.Solo,
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
                                if (threadHasReplies) {
                                    it.replies.forEach { reply ->
                                        ThreadTree(
                                            reply = reply, indentLevel = 2,
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
            //SkylineFragment(postFlow = postsFlow.asStateFlow(), {})
        }
    }

}
