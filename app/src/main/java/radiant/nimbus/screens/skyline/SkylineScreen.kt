package radiant.nimbus.screens.skyline

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.feed.GetFeedGeneratorQueryParams
import app.bsky.feed.GetFeedQueryParams
import com.atproto.repo.StrongRef
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.model.BskyPost
import radiant.nimbus.model.Skyline
import radiant.nimbus.screens.destinations.PostThreadScreenDestination
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import radiant.nimbus.ui.common.ComposerRole
import radiant.nimbus.ui.common.PostComposer
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
) {
    LaunchedEffect(Unit) {
        val uri = viewModel.state.feedUri
        if (uri != null) {

        } else{
            viewModel.getSkyline(mainViewModel.apiProvider)
        }

        mainViewModel.userPreferences?.savedFeeds?.pinned?.map { feedUri ->
            when(val response = mainViewModel.apiProvider.api.getFeedGenerator(GetFeedGeneratorQueryParams(feedUri))) {
                is AtpResponse.Failure -> {
                    Log.e("Skyline", "Error getting feed info: $response")}
                is AtpResponse.Success -> {
                    viewModel.pinnedFeeds += FeedTab(response.response.view.displayName, response.response.view.uri)
                }
            }
        }
    }
    SkylineView(
        navigator = navigator,
        viewModel = viewModel,
        refresh = {cursor ->
                viewModel.getSkyline(mainViewModel.apiProvider, cursor)
        },
        feedRefresh = { uri, cursor ->
            viewModel.getSkyline(mainViewModel.apiProvider, GetFeedQueryParams(uri), cursor)
        },
        apiProvider = mainViewModel.apiProvider,
        navBar = { mainViewModel.navBar?.let { it(0) } },
        mainButton = { onClicked ->
            OutlinedAvatar(url = mainViewModel.currentUser?.avatar.orEmpty(),
                modifier = Modifier.size(55.dp),
                onClicked = onClicked,
                outlineSize = 0.dp
            )
        }
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
    apiProvider: ApiProvider,
    refresh: (String?) -> Unit = {},
    feedRefresh: (AtUri, String?) -> Unit = {_,_ ->},
    navBar: @Composable () -> Unit = {},
    mainButton: @Composable ((() -> Unit) -> Unit)? = null,
){
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }



    LaunchedEffect(selectedTab) {
        if(selectedTab > 0) {
            viewModel.getSkyline(apiProvider, GetFeedQueryParams(
                viewModel.pinnedFeeds[selectedTab-1].uri,
                cursor = viewModel.pinnedFeeds[selectedTab-1].cursor
            ))
        }
    }
    ScreenBody(
        modifier = Modifier,
        topContent = {
            SkylineTopBar(viewModel.pinnedFeeds, tabIndex =  selectedTab,
                onChanged = {
                    selectedTab = it
                    if (selectedTab > 0) {
                        viewModel.getSkyline(
                            apiProvider, GetFeedQueryParams(
                                viewModel.pinnedFeeds[it - 1].uri,
                                cursor = viewModel.pinnedFeeds[it - 1].cursor
                            )
                        )
                    } else {
                        viewModel.getSkyline(apiProvider, viewModel.skylinePosts.value.cursor)
                    }
                },
                mainButton = mainButton,
                onButtonClicked = {

                }
            )
        },
        navBar = navBar,
        contentWindowInsets = WindowInsets.navigationBars,
    ) {insets ->
        val scope = rememberCoroutineScope()
        var repostClicked by remember { mutableStateOf(false)}
        var initialContent: BskyPost? by remember { mutableStateOf(null) }
        var showComposer by remember { mutableStateOf(false)}
        var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost)}
        val sheetState = rememberModalBottomSheetState()


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
                viewModel.feedPosts[viewModel.pinnedFeeds[selectedTab-1].uri]?.let { it: MutableStateFlow<Skyline> ->
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
                            feedRefresh(viewModel.pinnedFeeds[max(selectedTab-1, 0)].uri, cursor)
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
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showComposer = false
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.background,
                sheetState = sheetState,

                ){
                PostComposer(
                    role = composerRole,
                    modifier = Modifier.safeDrawingPadding().imePadding(),
                    initialContent = initialContent,
                    onCancel = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showComposer = false
                            }
                        }
                    },
                    onSend = {
                        apiProvider.createRecord(RecordUnion.MakePost(it))
                    }
                )
            }

        }


    }


}




@Composable
fun RepostQueryDialog(
    onDismissRequest: () -> Unit = {},
    onRepost: () -> Unit = {},
    onQuotePost: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true,),
    ) {
        BackHandler {
            onDismissRequest()
        }
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.padding(36.dp)
        ) {
            TextButton(
                onClick = {
                    onRepost()
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Repeat, contentDescription = null)
                Text(text = "Repost")
            }
            TextButton(
                onClick = {
                    onQuotePost()
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Icon(imageVector = Icons.Default.FormatQuote, contentDescription = null)
                Text(text = "Quote Post")
            }

            Button(
                onClick = { onDismissRequest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    }
}

