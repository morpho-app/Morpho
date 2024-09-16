package com.morpho.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.paging.PagingData
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uidata.ContentHandling
import com.morpho.app.model.uidata.FeedUpdate
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.elements.WrappedLazyColumn
import com.morpho.app.ui.post.PlaceholderSkylineItem
import com.morpho.app.ui.post.PostFragment
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

typealias OnPostClicked = (AtUri) -> Unit


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SkylineFragment (
    modifier: Modifier = Modifier,
    onItemClicked: OnPostClicked,
    onProfileClicked: (AtIdentifier) -> Unit = {},
    onPostButtonClicked: () -> Unit = {},
    onReplyClicked: (BskyPost) -> Unit = { },
    onRepostClicked: (BskyPost) -> Unit = { },
    onLikeClicked: (StrongRef) -> Unit = { },
    onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
    onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    getContentHandling: (BskyPost) -> List<ContentHandling> = { listOf() },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isProfileFeed: Boolean = false,
    debuggable: Boolean = false,
    feedUpdate: StateFlow<FeedUpdate<MorphoDataItem.FeedItem>>,
) {
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val state = feedUpdate.collectAsState()
    val pager = remember { when(state.value) {
        is FeedUpdate.Feed -> (state.value as FeedUpdate.Feed<MorphoDataItem.FeedItem>).feed
        is FeedUpdate.Peek -> null
        else -> null
    } }

    val data = pager?.collectAsLazyPagingItems()
    val pagerState = pager?.collectAsState(
        if(data != null) PagingData.from(data.itemSnapshotList.items) else PagingData.empty()
    )


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


    fun refreshPull() = data?.refresh()

    val refreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullRefreshState(refreshing, ::refreshPull)


    ConstraintLayout(
        modifier = if(isProfileFeed) {
            Modifier
                .fillMaxSize().systemBarsPadding()

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
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = if(isProfileFeed) {
                //contentPadding
                PaddingValues(
                    bottom = contentPadding.calculateBottomPadding(),
//                    top = WindowInsets.safeContent.only(WindowInsetsSides.Top).asPaddingValues()
//                        .calculateTopPadding()
                    top = contentPadding.calculateTopPadding()
                )
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
            if(false) {
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
            when(val loadState = data?.loadState?.refresh) {
                is LoadStateError -> {
                    item { Text("Error: ${loadState.error}") }
                    item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(onClick = { data.retry() }) {
                            Text("Retry")
                        } } }
                }

                is LoadStateNotLoading -> {
                    items(
                        data.itemCount,
                        key = data.itemKey { it.key }
                    ) { index ->
                        when(val item = data[index]) {
                            is MorphoDataItem.Thread -> {
                                SkylineThreadFragment(
                                    thread = item.thread,
                                    modifier = if(debuggable) Modifier.border(1.dp, Color.White) else Modifier
                                        .fillMaxWidth()
                                        //.padding(horizontal = 4.dp),
                                        .padding(vertical = 2.dp, horizontal = 4.dp),
                                    onItemClicked = onItemClicked,
                                    onProfileClicked = onProfileClicked,
                                    onUnClicked = onUnClicked,
                                    onRepostClicked = onRepostClicked,
                                    onReplyClicked = onReplyClicked,
                                    onMenuClicked = onMenuClicked,
                                    onLikeClicked = onLikeClicked,
                                    getContentHandling = getContentHandling,
                                    debuggable = debuggable,
                                )
                            }
                            is MorphoDataItem.Post -> {
                                PostFragment(
                                    modifier = if(debuggable) Modifier.border(1.dp, Color.Blue) else Modifier
                                        .fillMaxWidth()
                                        //.padding(horizontal = 4.dp),
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
                                    getContentHandling = getContentHandling,
                                )
                            }

                            else -> {
                                PlaceholderSkylineItem(
                                    modifier = if(debuggable) Modifier.border(1.dp, Color.Black) else Modifier
                                        .fillMaxWidth()
                                        //.padding(horizontal = 4.dp),
                                        .padding(vertical = 2.dp, horizontal = 4.dp),
                                    elevate = true,
                                )
                            }
                        }
                    }
                }
                else -> { item { LoadingCircle() } }
            }
            if (data?.loadState?.append == LoadStateLoading) item { LoadingCircle() }
        }
        if (scrolledDownSome) {

            OutlinedIconButton(
                onClick = {
                    scope.launch {
                        //refreshPull()
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
