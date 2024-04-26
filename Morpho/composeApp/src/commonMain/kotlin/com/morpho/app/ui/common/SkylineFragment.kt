package com.morpho.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uidata.AtCursor
import com.morpho.app.model.uistate.ContentCardState
import com.morpho.app.model.uistate.ContentLoadingState
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.elements.WrappedLazyColumn
import com.morpho.app.ui.post.PostFragment
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

typealias OnPostClicked = (AtUri) -> Unit


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T: MorphoDataItem> SkylineFragment (
    content: StateFlow<ContentCardState<T>>,
    modifier: Modifier = Modifier,
    onItemClicked: OnPostClicked,
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onPostButtonClicked: () -> Unit = {},
    refresh: (AtCursor) -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions) -> Unit = { },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isProfileFeed: Boolean = false,
) {
    val currentRefresh by rememberUpdatedState(refresh)


    val state = content.collectAsState()
    val loading = state.value.loadingState
    val cursor by rememberUpdatedState(state.value.feed.cursor)

    val coroutineScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    val listState: LazyListState = rememberLazyListState()

    val data = remember(loading, state, cursor, refreshing) {
        state.value.feed
    }
    val scrolledDownSome by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 5
        }
    }

    val scrolledDownLots by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 20
        }
    }

    fun refreshPull() = coroutineScope.launch {
        refreshing = true
        launch { currentRefresh(null) }
            .invokeOnCompletion { refreshing = false }

    }

    LaunchedEffect(
        data.items.isNotEmpty() &&
            loading == ContentLoadingState.Idle &&
            !listState.canScrollForward &&
            !refreshing &&
            scrolledDownSome
    ) {
        currentRefresh(cursor)
    }


    val refreshState = rememberPullRefreshState(refreshing, ::refreshPull)



    ConstraintLayout(
        modifier = if(isProfileFeed) {
            Modifier
                .fillMaxWidth()
                .systemBarsPadding()

        } else {
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
        },
    ) {
        val (scrollButton, postButton, skyline, refreshIndicator) = createRefs()
        val leftGuideline = createGuidelineFromStart(40.dp)
        val rightGuideline = createGuidelineFromEnd(40.dp)
        val buttonGuideline = createGuidelineFromBottom(100.dp)


        WrappedLazyColumn(
            modifier = modifier
                .pullRefresh(refreshState)
                .constrainAs(skyline) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            //flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = if(isProfileFeed) {
                contentPadding
            } else {
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
                data.items, key = {it.hashCode()},
                contentType = {
                    when(it) {
                        is MorphoDataItem.Post -> MorphoDataItem.Post::class
                        is MorphoDataItem.Thread -> MorphoDataItem.Thread::class
                        else -> {}
                    }
                }
            ) {item ->
                when(item) {
                    is MorphoDataItem.Thread -> {
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
                    is MorphoDataItem.Post -> {
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
        if (scrolledDownSome) {

            OutlinedIconButton(
                onClick = {
                    coroutineScope.launch {
                        refreshPull()
                        if (scrolledDownLots) {
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
                    if (scrolledDownLots) {
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
        PullRefreshIndicator(refreshing, refreshState, Modifier.constrainAs(refreshIndicator) {
            top.linkTo(parent.top)
            centerHorizontallyTo(parent)
        }, backgroundColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary)
    }

}
