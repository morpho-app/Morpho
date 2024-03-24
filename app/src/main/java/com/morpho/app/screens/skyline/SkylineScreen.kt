package com.morpho.app.screens.skyline

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.feed.GetFeedQuery
import com.atproto.repo.StrongRef
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import com.morpho.app.MainViewModel
import kotlinx.serialization.Serializable
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordType
import com.morpho.butterfly.model.RecordUnion
import com.morpho.app.components.ScreenBody
import com.morpho.app.extensions.activityViewModel
import com.morpho.app.model.BskyPost
import com.morpho.app.model.DraftPost
import com.morpho.app.model.Skyline
import com.morpho.app.screens.destinations.MyProfileScreenDestination
import com.morpho.app.ui.common.BottomSheetPostComposer
import com.morpho.app.ui.common.ComposerRole
import com.morpho.app.ui.common.RepostQueryDialog
import com.morpho.app.ui.common.SkylineFragment
import com.morpho.app.ui.common.SkylineTopBar
import com.morpho.app.ui.elements.OutlinedAvatar
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalKStoreApi::class)
@RootNavGraph(start = true)
@Destination
@Composable
fun SkylineScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: SkylineViewModel = hiltViewModel(),
    tabIndex: Int = 0,
) {
    LaunchedEffect(Unit) {
        val uri = viewModel.state.feedUri
        if (uri != null) {

        } else {
            viewModel.hasPosts().await()
            mainViewModel.userPreferences?.feedViewPrefs?.get("home")?.let {
                viewModel.getSkyline(
                    null,
                    prefs = it
                )
            }
            mainViewModel.pinnedFeeds.forEach {
            viewModel.getSkyline(
                GetFeedQuery(
                    it.uri,
                    cursor = it.cursor
                )
            )
            }
        }
        mainViewModel.getUnreadCount()
    }
    viewModel.pinnedFeeds = mainViewModel.pinnedFeeds
    SkylineView(
        navigator = navigator,
        viewModel = viewModel,
        refresh = { cursor ->

            mainViewModel.userPreferences?.feedViewPrefs?.get("home")?.let {
                viewModel.getSkyline(
                    cursor,
                    prefs = it
                )
            }
            mainViewModel.getUnreadCount()
        },
        feedRefresh = { uri, cursor -> viewModel
            .getSkyline(GetFeedQuery(uri), cursor)
            mainViewModel.getUnreadCount()
                      },
        navBar = { mainViewModel.navBar?.let { it(0) } },
        mainButton = { onClicked ->
            OutlinedAvatar(url = mainViewModel.currentUser?.avatar.orEmpty(),
                modifier = Modifier.size(55.dp),
                onClicked = onClicked,
                outlineSize = 0.dp
            )
        },
        pinnedFeeds = viewModel.pinnedFeeds,
        tabIndex = tabIndex,
    )
}

@Serializable
data class FeedTab(
    val title: String,
    val uri: AtUri,
    var cursor: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkylineView(
    navigator: DestinationsNavigator,
    viewModel: SkylineViewModel,
    pinnedFeeds: List<FeedTab> = listOf(),
    refresh: (String?) -> Unit = {},
    feedRefresh: (AtUri, String?) -> Unit = { _, _ ->},
    navBar: @Composable () -> Unit = {},
    mainButton: @Composable ((() -> Unit) -> Unit)? = null,
    tabIndex: Int = 0,
){
    var selectedTab by rememberSaveable { mutableIntStateOf(tabIndex) }
    var repostClicked by remember { mutableStateOf(false)}
    var initialContent: BskyPost? by remember { mutableStateOf(null) }
    var showComposer by remember { mutableStateOf(false)}
    var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost)}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Probably pull this farther up,
    //      but this means if you don't explicitly cancel you don't lose the post
    var draft by remember{ mutableStateOf(DraftPost()) }
    val scope = rememberCoroutineScope()

    val onProfileClicked:(actor: AtIdentifier, navigator: DestinationsNavigator) -> Unit = remember { return@remember viewModel::onProfileClicked }
    val onItemClicked:(uri: AtUri, navigator: DestinationsNavigator) -> Unit = remember { return@remember viewModel::onItemClicked }
    val onUnClicked:(type: RecordType, rkey: AtUri) -> Unit = remember { return@remember viewModel.apiProvider::deleteRecord }
    val onLikeClicked:(ref: StrongRef) -> Unit = remember { return@remember {
        viewModel.apiProvider.createRecord(RecordUnion.Like(it))
    } }
    val onReplyClicked:(post: BskyPost) -> Unit = remember {return@remember {
        initialContent = it
        composerRole = ComposerRole.Reply
        showComposer = true
    }}

    val onRepostClicked:(post: BskyPost) -> Unit = remember {return@remember {
        initialContent = it
        repostClicked = true
    }}

    val onPostButtonClicked:() -> Unit = remember {return@remember {
        composerRole = ComposerRole.StandalonePost
        showComposer = true
    }}

    val onTabChanged: (index: Int) -> Unit = remember {
        return@remember {index ->
            selectedTab = index
            if (selectedTab > 0) {
                viewModel.getSkyline(
                    GetFeedQuery(
                        viewModel.pinnedFeeds[max(index, 1)].uri,
                        cursor = viewModel.pinnedFeeds[max(index, 1)].cursor
                    )
                )
            } else {
                viewModel.getSkyline(viewModel.skylinePosts.value.cursor)
            }
        }
    }


    LaunchedEffect(Clock.System.now().epochSeconds % 60L == 0L) {
        viewModel.hasPosts().await()
    }

    LaunchedEffect(selectedTab) {
        if(selectedTab > 0) {
            viewModel.getSkyline(GetFeedQuery(
                pinnedFeeds[selectedTab].uri,
                cursor = pinnedFeeds[selectedTab].cursor
            ))
        }  else {
            viewModel.getSkyline(viewModel.skylinePosts.value.cursor)
        }
    }
    ScreenBody(
        modifier = Modifier,
        topContent = {
            SkylineTopBar(pinnedFeeds,
                modifier = Modifier.padding(start = 50.dp, top = 0.dp, bottom = 0.dp, end = 0.dp),
                tabIndex =  selectedTab,
                onChanged = { onTabChanged(it) },
                mainButton = mainButton,
                onButtonClicked = {
                    navigator.navigate(MyProfileScreenDestination)
                }
            )
        },
        navBar = navBar,
        contentWindowInsets = WindowInsets.navigationBars,
    ) {insets ->
        when(selectedTab) {
            0 -> {
                SkylineFragment(
                    postFlow = viewModel.skylinePosts,
                    onItemClicked = {onItemClicked(it, navigator)},
                    onProfileClicked = {onProfileClicked(it, navigator)},
                    refresh = refresh,
                    contentPadding = insets,
                    onUnClicked = {type, rkey ->   onUnClicked(type, rkey)},
                    onRepostClicked = {onRepostClicked(it)},
                    onReplyClicked = {onReplyClicked(it)},
                    onMenuClicked = {
                    },
                    onLikeClicked = {onLikeClicked(it)},
                    onPostButtonClicked = {onPostButtonClicked()}
                )
            }
            else -> {
                viewModel.feedPosts[viewModel.pinnedFeeds[min(selectedTab, viewModel.pinnedFeeds.lastIndex)].uri]?.let { it: MutableStateFlow<Skyline> ->
                    SkylineFragment(
                        postFlow = it,
                        onItemClicked = {onItemClicked(it, navigator)},
                        onProfileClicked = {onProfileClicked(it, navigator)},
                        refresh = { cursor->
                            feedRefresh(viewModel.pinnedFeeds[min(selectedTab, viewModel.pinnedFeeds.lastIndex)].uri, cursor)
                        },
                        contentPadding = insets,
                        onUnClicked = {type, rkey ->   onUnClicked(type, rkey)},
                        onRepostClicked = {onRepostClicked(it)},
                        onReplyClicked = {onReplyClicked(it)},
                        onMenuClicked = {
                        },
                        onLikeClicked = {onLikeClicked(it)},
                        onPostButtonClicked = {onPostButtonClicked()}
                    )
                }
            }
        }
        if(repostClicked) {
            RepostQueryDialog(
                onDismissRequest = {
                    showComposer = false
                    repostClicked = false

                },
                onRepost = {
                    repostClicked = false
                    initialContent?.let { post ->
                        RecordUnion.Repost(
                            StrongRef(post.uri,post.cid)
                        )
                    }?.let { viewModel.apiProvider.createRecord(it) }
                },
                onQuotePost = {
                    showComposer = true
                    repostClicked = false
                }
            )
        }
        if(showComposer) {
            BottomSheetPostComposer(
                onDismissRequest = { showComposer = false },
                sheetState = sheetState,
                role = composerRole,
                //modifier = Modifier.padding(insets),
                initialContent = initialContent,
                draft = draft,
                onCancel = {
                    showComposer = false
                    draft = DraftPost()
                           },
                onSend = {
                    viewModel.apiProvider.createRecord(RecordUnion.MakePost(it))
                    showComposer = false
                         },
                onUpdate = { draft = it }
            )

        }


    }


}

