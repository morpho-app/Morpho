package com.morpho.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atproto.repo.StrongRef
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import com.morpho.app.components.ScreenBody
import com.morpho.app.model.BskyPost
import com.morpho.app.model.Skyline
import com.morpho.app.model.SkylineItem
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.post.PostFragment
import com.morpho.app.ui.post.testPost
import com.morpho.app.ui.theme.MorphoTheme

typealias OnPostClicked = (AtUri) -> Unit


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun SkylineFragment (
    postFlow: StateFlow<Skyline>,
    modifier: Modifier = Modifier,
    onItemClicked: OnPostClicked,
    onProfileClicked: (AtIdentifier) -> Unit = {},
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
    var refreshing by remember { mutableStateOf(false) }
    val listState: LazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if(listState.firstVisibleItemIndex == 0 && !isProfileFeed) listState.animateScrollToItem(0, 50)
    }
    LaunchedEffect(!listState.canScrollForward) {
        refresh(postList.cursor)
    }
    fun refreshPull() = coroutineScope.launch {
        refreshing = true
        launch { refresh(null) }
        delay(200)
        refreshing = false
    }

    val refreshState = rememberPullRefreshState(refreshing, ::refreshPull)

    val scrolledDownBy by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    ConstraintLayout(
        modifier = if(isProfileFeed) {
            Modifier
                .fillMaxWidth()
                .systemBarsPadding()

        }   else {
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
        },
    ) {
        val (scrollButton, postButton, skyline, refreshIndicator) = createRefs()
        val leftGuideline = createGuidelineFromStart(40.dp)
        val rightGuideline = createGuidelineFromEnd(40.dp)
        val buttonGuideline = createGuidelineFromBottom(100.dp)


        LazyColumn(
            modifier = modifier
                .pullRefresh(refreshState)
                .constrainAs(skyline) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            //flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
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
                            onClick = { refreshPull() },
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
            items(
                postList.posts, key = {it.hashCode()},
                contentType = {
                    when(it) {
                        is SkylineItem.PostItem -> SkylineItem.PostItem::class
                        is SkylineItem.ThreadItem -> SkylineItem.ThreadItem::class
                        else -> {}
                    }
                }
            ) {item ->
                when(item) {
                    is SkylineItem.ThreadItem -> {
                        SkylineThreadFragment(
                            thread = item.thread,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            onItemClicked = onItemClicked,
                            onProfileClicked = onProfileClicked,
                            onUnClicked = onUnClicked,
                            onRepostClicked = onRepostClicked,
                            onReplyClicked = onReplyClicked,
                            onMenuClicked = onMenuClicked,
                            onLikeClicked = onLikeClicked,
                        )
                    }
                    is SkylineItem.PostItem -> {
                        PostFragment(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            post = item.post,
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

                    else -> {}
                }
            }
        }
        if (scrolledDownBy > 5 || postList.hasNewPosts) {

            OutlinedIconButton(
                onClick = {
                    coroutineScope.launch {
                        launch { refresh(null) }
                        if (scrolledDownBy > 20) {
                            listState.scrollToItem(0)
                        } else {
                            listState.animateScrollToItem(0)
                        }
                    }
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        1.dp
                    ).copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .constrainAs(scrollButton) {
                        centerAround(buttonGuideline)
                        centerAround(leftGuideline)
                    }
                    .size(50.dp)
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
                    if (postList.hasNewPosts) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(10.dp)
                                .offset(12.dp, (-12).dp)
                        ) {
                            //Icon(imageVector = Icons.Default.Circle, contentDescription = "New posts available")
                        }
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
        PullRefreshIndicator(refreshing, refreshState, Modifier.constrainAs(refreshIndicator) {
            top.linkTo(parent.top)
            centerHorizontallyTo(parent)
        }, backgroundColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary)
    }

}


@Composable
@Preview
fun PreviewSkyline() {
    MorphoTheme {
        ScreenBody(modifier = Modifier.height(1000.dp)) {
            val posts = mutableListOf<SkylineItem>()
            for (i in 1..10) {
                posts.add(SkylineItem.PostItem(testPost))
            }
            val postsFlow = MutableStateFlow(Skyline(posts, null))
            //SkylineFragment(postFlow = postsFlow.asStateFlow(), {})
        }
    }

}
