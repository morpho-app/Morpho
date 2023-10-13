package radiant.nimbus.screens.postthread

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.persistentListOf
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.model.BskyPostThread
import radiant.nimbus.screens.destinations.PostThreadScreenDestination
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import radiant.nimbus.screens.skyline.FeedTab
import radiant.nimbus.ui.common.SkylineTopBar
import radiant.nimbus.ui.thread.ThreadFragment

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
        navigator.navigateUp()
    }
    if(showLoadingScreen) {
            viewModel.loadThread(uri, mainViewModel.apiProvider) {
                showLoadingScreen = false
            }
    } else {
        if (!viewModel.state.isBlocked || !viewModel.state.notFound || viewModel.thread != null ) {
            viewModel.thread?.let {
                PostThreadView(
                    thread = it,
                    apiProvider = mainViewModel.apiProvider,
                    navigator = navigator,
                    navBar = { mainViewModel.navBar?.let { it(5) } },
                )
            }
        }
    }

}

@Composable
fun PostThreadView(
    thread: BskyPostThread,
    apiProvider: ApiProvider,
    navigator: DestinationsNavigator,
    navBar: @Composable () -> Unit = {},
    tabList: List<FeedTab> = persistentListOf(),
){
    ScreenBody(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        topContent = {
            SkylineTopBar(tabList)
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
                apiProvider.createRecord(RecordUnion.Repost(it))
                /* TODO: Add dialog/quote post option */
            },
            onReplyClicked = { },
            onMenuClicked = { },
            onLikeClicked = {
                apiProvider.createRecord(RecordUnion.Like(it))
            },
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PostThreadPreview(){
    //PostThreadView()
}