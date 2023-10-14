package radiant.nimbus.screens.skyline

import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.feed.GetFeedQueryParams
import com.atproto.repo.StrongRef
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.DraftPost
import radiant.nimbus.model.Skyline
import radiant.nimbus.screens.destinations.MyProfileScreenDestination
import radiant.nimbus.screens.destinations.PostThreadScreenDestination
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import radiant.nimbus.ui.common.BottomSheetPostComposer
import radiant.nimbus.ui.common.ComposerRole
import radiant.nimbus.ui.common.RepostQueryDialog
import radiant.nimbus.ui.common.SkylineFragment
import radiant.nimbus.ui.common.SkylineTopBar
import radiant.nimbus.ui.elements.OutlinedAvatar
import kotlin.math.max

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
            viewModel.getSkyline(mainViewModel.apiProvider)
            mainViewModel.pinnedFeeds.forEach {
                viewModel.getSkyline(mainViewModel.apiProvider,
                    GetFeedQueryParams(
                        it.uri,
                        cursor = it.cursor
                    )
                )
            }
        }
    }
    SkylineView(
        navigator = navigator,
        viewModel = viewModel,
        refresh = {cursor -> viewModel.getSkyline(mainViewModel.apiProvider, cursor) },
        feedRefresh = { uri, cursor -> viewModel
            .getSkyline(mainViewModel.apiProvider, GetFeedQueryParams(uri), cursor) },
        apiProvider = mainViewModel.apiProvider,
        navBar = { mainViewModel.navBar?.let { it(0) } },
        mainButton = { onClicked ->
            OutlinedAvatar(url = mainViewModel.currentUser?.avatar.orEmpty(),
                modifier = Modifier.size(55.dp),
                onClicked = onClicked,
                outlineSize = 0.dp
            )
        },
        pinnedFeeds = mainViewModel.pinnedFeeds,
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
    pinnedFeeds: List<FeedTab> = persistentListOf(),
    apiProvider: ApiProvider,
    refresh: (String?) -> Unit = {},
    feedRefresh: (AtUri, String?) -> Unit = {_,_ ->},
    navBar: @Composable () -> Unit = {},
    mainButton: @Composable ((() -> Unit) -> Unit)? = null,
    tabIndex: Int = 0,
){
    var selectedTab by rememberSaveable { mutableIntStateOf(tabIndex) }

    var repostClicked by remember { mutableStateOf(false)}
    var initialContent: BskyPost? by remember { mutableStateOf(null) }
    var showComposer by remember { mutableStateOf(false)}
    var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost)}
    val sheetState = rememberModalBottomSheetState()
    // Probably pull this farther up,
    //      but this means if you don't explicitly cancel you don't lose the post
    var draft by remember{ mutableStateOf(DraftPost()) }


    LaunchedEffect(selectedTab) {
        if(selectedTab > 0) {
            viewModel.getSkyline(apiProvider, GetFeedQueryParams(
                pinnedFeeds[selectedTab-1].uri,
                cursor = pinnedFeeds[selectedTab-1].cursor
            ))
        }
    }
    ScreenBody(
        modifier = Modifier,
        topContent = {
            SkylineTopBar(pinnedFeeds, tabIndex =  selectedTab,
                onChanged = {
                    selectedTab = it
                    if (selectedTab > 0) {
                        viewModel.getSkyline(
                            apiProvider, GetFeedQueryParams(
                                pinnedFeeds[max(it-1, 0)].uri,
                                cursor = pinnedFeeds[max(it-1, 0)].cursor
                            )
                        )
                    } else {
                        viewModel.getSkyline(apiProvider, viewModel.skylinePosts.value.cursor)
                    }
                },
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
                    navigator = navigator,
                    postFlow = viewModel.skylinePosts,
                    onItemClicked = {
                        navigator.navigate(PostThreadScreenDestination(it))
                    },
                    onProfileClicked = {
                        navigator.navigate(ProfileScreenDestination(it))
                    },
                    refresh = refresh,
                    contentPadding = insets,
                    onUnClicked = {type, rkey ->  apiProvider.deleteRecord(type, rkey)},
                    onRepostClicked = {
                        initialContent = it
                        composerRole = ComposerRole.QuotePost
                        repostClicked = true
                    },
                    onReplyClicked = {
                        initialContent = it
                        composerRole = ComposerRole.Reply
                        showComposer = true
                    },
                    onMenuClicked = { },
                    onLikeClicked = {
                        apiProvider.createRecord(RecordUnion.Like(it))
                    },
                    onPostButtonClicked = {
                        composerRole = ComposerRole.StandalonePost
                        showComposer = true
                    }
                )
            }
            else -> {
                viewModel.feedPosts[pinnedFeeds[max(selectedTab-1, 0)].uri]?.let { it: MutableStateFlow<Skyline> ->
                    SkylineFragment(
                        navigator = navigator,
                        postFlow = it,
                        onItemClicked = {
                            navigator.navigate(PostThreadScreenDestination(it))
                        },
                        onProfileClicked = {
                            navigator.navigate(ProfileScreenDestination(it))
                        },
                        refresh = { cursor->
                            feedRefresh(pinnedFeeds[max(selectedTab-1, 0)].uri, cursor)
                        },
                        contentPadding = insets,
                        onUnClicked = {type, rkey ->  apiProvider.deleteRecord(type, rkey)},
                        onRepostClicked = {
                            initialContent = it
                            repostClicked = true
                        },
                        onReplyClicked = {
                            initialContent = it
                            composerRole = ComposerRole.Reply
                            showComposer = true
                        },
                        onMenuClicked = { },
                        onLikeClicked = {
                            apiProvider.createRecord(RecordUnion.Like(it))
                        },
                        onPostButtonClicked = {
                            composerRole = ComposerRole.StandalonePost
                            showComposer = true
                        }
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
                    }?.let { apiProvider.createRecord(it) }
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
                modifier = Modifier.padding(insets),
                initialContent = initialContent,
                draft = draft,
                onCancel = { showComposer = false },
                onSend = { apiProvider.createRecord(RecordUnion.MakePost(it)) },
                onUpdate = { draft = it }
            )

        }


    }


}

