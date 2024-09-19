package com.morpho.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import com.atproto.repo.StrongRef
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.bluesky.MorphoDataItem
import com.morpho.app.model.uidata.AuthorFeedUpdate
import com.morpho.app.model.uidata.FeedUpdate
import com.morpho.app.model.uidata.UIUpdate
import com.morpho.app.ui.elements.MenuOptions
import com.morpho.app.ui.elements.WrappedLazyColumn
import com.morpho.app.ui.post.PlaceholderSkylineItem
import com.morpho.app.ui.post.PostFragment
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.ContentHandling
import com.morpho.butterfly.model.RecordType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

typealias OnPostClicked = (AtUri) -> Unit


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
inline fun <reified U: UIUpdate> SkylineFragment (
    modifier: Modifier = Modifier,
    noinline onItemClicked: OnPostClicked,
    noinline onProfileClicked: (AtIdentifier) -> Unit = {},
    crossinline onPostButtonClicked: () -> Unit = {},
    noinline onReplyClicked: (BskyPost) -> Unit = { },
    noinline onRepostClicked: (BskyPost) -> Unit = { },
    noinline onLikeClicked: (StrongRef) -> Unit = { },
    noinline onMenuClicked: (MenuOptions, BskyPost) -> Unit = { _, _ -> },
    noinline onUnClicked: (type: RecordType, uri: AtUri) -> Unit = { _, _ -> },
    noinline getContentHandling: (BskyPost) -> List<ContentHandling> = { listOf() },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isProfileFeed: Boolean = false,
    debuggable: Boolean = false,
    feedUpdate: Flow<U>,
) {
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val state = feedUpdate.filterIsInstance<U>().collectAsState(
        when(feedUpdate) {
            is AuthorFeedUpdate -> AuthorFeedUpdate.Empty
            is FeedUpdate<*> -> FeedUpdate.Empty
            else -> UIUpdate.Empty
        }
    )
    val data =  when(state.value) {
        is AuthorFeedUpdate.Feed -> (state.value as AuthorFeedUpdate.Feed).feed.collectAsLazyPagingItems()
        is AuthorFeedUpdate.Feeds -> (state.value as AuthorFeedUpdate.Feeds).feed.collectAsLazyPagingItems()
        is AuthorFeedUpdate.Likes -> (state.value as AuthorFeedUpdate.Likes).feed.collectAsLazyPagingItems()
        is AuthorFeedUpdate.Lists -> (state.value as AuthorFeedUpdate.Lists).feed.collectAsLazyPagingItems()
        is FeedUpdate.Feed -> (state.value as FeedUpdate.Feed).feed.collectAsLazyPagingItems()
        else -> null
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


    val refreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullRefreshState(refreshing, {data?.refresh()})


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
                            onClick = {data?.refresh()},
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
                is LoadStateLoading -> { item { LoadingCircle() } }
                else -> { if(data != null) {
                    items(
                        data.itemCount,
                        key = data.itemKey {when(it) {
                            is MorphoDataItem.FeedItem -> it.key
                            is MorphoDataItem.Post -> it.key
                            is MorphoDataItem.Thread -> it.key
                            else -> it.hashCode()
                        } }
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
                    } }
                }
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
