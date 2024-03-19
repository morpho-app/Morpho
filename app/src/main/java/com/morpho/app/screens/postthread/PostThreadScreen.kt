package com.morpho.app.screens.postthread

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.atproto.repo.StrongRef
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.morpho.app.MainViewModel
import com.morpho.app.butterfly
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.model.RecordUnion
import com.morpho.app.components.Center
import com.morpho.app.components.ScreenBody
import com.morpho.app.extensions.activityViewModel
import com.morpho.app.model.BskyPost
import com.morpho.app.model.BskyPostThread
import com.morpho.app.model.DraftPost
import com.morpho.app.screens.destinations.PostThreadScreenDestination
import com.morpho.app.screens.destinations.ProfileScreenDestination
import com.morpho.app.screens.destinations.SkylineScreenDestination
import com.morpho.app.screens.skyline.FeedTab
import com.morpho.app.ui.common.BottomSheetPostComposer
import com.morpho.app.ui.common.ComposerRole
import com.morpho.app.ui.common.RepostQueryDialog
import com.morpho.app.ui.common.SkylineTopBar
import com.morpho.app.ui.thread.ThreadFragment

@Destination
@Composable
fun PostThreadScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: PostThreadViewModel = hiltViewModel(),
    uri: AtUri,
) {
    var showLoadingScreen by rememberSaveable { mutableStateOf(true) }
    BackHandler {
        navigator.popBackStack()
    }
    if((uri != viewModel.thread?.post?.uri) || showLoadingScreen) {
        showLoadingScreen = true
        ScreenBody(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            topContent = {
                SkylineTopBar(
                    tabList = mainViewModel.pinnedFeeds,
                    mainButton = {
                        IconButton(onClick = { it() },
                            modifier = Modifier
                                .padding(bottom = 5.dp, top = 5.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .size(30.dp)
                            )
                        }
                    },
                    onButtonClicked = {
                        navigator.popBackStack()
                    },
                    onChanged = {
                        if(it < mainViewModel.pinnedFeeds.size) navigator.navigate(SkylineScreenDestination(it))
                    },
                )
            },
            navBar = { mainViewModel.navBar?.let { it(5) } },
            contentWindowInsets = WindowInsets.navigationBars,
        ) {insets ->
            Center {
                //CircularProgressIndicator()
                Text("Loading")
            }
        }
        viewModel.loadThread(uri) {
            showLoadingScreen = false
        }
    } else {
        if (!viewModel.state.isBlocked || !viewModel.state.notFound || viewModel.thread != null ) {
            viewModel.thread?.let {
                PostThreadView(
                    thread = it,
                    navigator = navigator,
                    navBar = { mainViewModel.navBar?.let { it(5) } },
                    tabList = mainViewModel.pinnedFeeds
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostThreadView(
    thread: BskyPostThread,
    navigator: DestinationsNavigator,
    navBar: @Composable () -> Unit = {},
    tabList: List<FeedTab> = listOf(),
){
    val apiProvider = LocalContext.current.butterfly
    var repostClicked by remember { mutableStateOf(false)}
    var initialContent: BskyPost? by remember { mutableStateOf(null) }
    var showComposer by remember { mutableStateOf(false)}
    var composerRole by remember { mutableStateOf(ComposerRole.StandalonePost)}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Probably pull this farther up,
    //      but this means if you don't explicitly cancel you don't lose the post
    var draft by remember{ mutableStateOf(DraftPost()) }
    ScreenBody(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topContent = {
            SkylineTopBar(tabList,
                mainButton = {
                    IconButton(onClick = { it() },
                        modifier = Modifier
                            .padding(bottom = 5.dp, top = 5.dp)
                        ) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .size(30.dp)
                        )
                    }
                },
                onButtonClicked = {
                    navigator.navigateUp()
                },
                onChanged = {
                    if(it < tabList.size) navigator.navigate(SkylineScreenDestination(it))
                },
            )
        },
        navBar = navBar,
        contentWindowInsets = WindowInsets.navigationBars,
    ) {insets ->
        ThreadFragment(thread = thread,
            contentPadding = insets,
            onItemClicked = {
                navigator.navigate(PostThreadScreenDestination(it))
            },
            onProfileClicked = {
                navigator.navigate(ProfileScreenDestination(it))
            },
            onUnClicked = {type, uri ->  apiProvider.deleteRecord(type, uri = uri)},
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
        )
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
                //modifier = Modifier.padding(insets),
                initialContent = initialContent,
                draft = draft,
                onCancel = {
                    showComposer = false
                    draft = DraftPost()
                },
                onSend = {
                    apiProvider.createRecord(RecordUnion.MakePost(it))
                    showComposer = false
                },
                onUpdate = { draft = it }
            )

        }
    }
}

@Composable
@Preview(showBackground = true)
fun PostThreadPreview(){
    //PostThreadView()
}