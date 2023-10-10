package radiant.nimbus.screens.skyline

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.feed.GetFeedQueryParams
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.screens.destinations.PostThreadScreenDestination
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import radiant.nimbus.ui.common.SkylineFragment

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
            viewModel.getSkyline(
                mainViewModel.apiProvider,
                GetFeedQueryParams(uri)
                )
        } else{
            viewModel.getSkyline(mainViewModel.apiProvider)
        }
    }
    SkylineView(
        navigator = navigator,
        viewModel = viewModel,
        refresh = {cursor ->
                viewModel.getSkyline(mainViewModel.apiProvider, cursor)
        },
        apiProvider = mainViewModel.apiProvider,
        navBar = { mainViewModel.navBar?.let { it() } },
    )
}

@Composable
fun SkylineView(
    navigator: DestinationsNavigator,
    viewModel: SkylineViewModel,
    apiProvider: ApiProvider,
    refresh: (String?) -> Unit = {},
    navBar: @Composable () -> Unit = {},
){
    val scope = rememberCoroutineScope()
    val likeKeys = remember { mutableStateMapOf(Pair<AtUri, String?>(AtUri(""), "")) }
    val repostKeys = remember { mutableStateMapOf(Pair<AtUri, String?>(AtUri(""), "")) }

    ScreenBody(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .heightIn(0.dp, 20000.dp),
        topContent = {},
        navBar = navBar

    ) {

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
                modifier = Modifier,
                onUnClicked = {type, rkey ->  apiProvider.deleteRecord(type, rkey)},
                onRepostClicked = {
                    /* TODO: Add dialog/quote post option */
                     scope.launch {
                         repostKeys[it.uri] = viewModel.createRecord(
                            record = RecordUnion.Repost(it),
                            apiProvider = apiProvider
                        ).await()
                    }
                                  },
                onReplyClicked = { },
                onMenuClicked = { },
                onLikeClicked = {
                    scope.launch {
                        likeKeys[it.uri] = viewModel.createRecord(
                            record = RecordUnion.Like(it),
                            apiProvider = apiProvider
                        ).await()
                    }
                },
                lKeys = snapshotFlow { likeKeys },
                rpKeys = snapshotFlow { repostKeys },

            )
    }


}

@Composable
@Preview(showBackground = true)
fun SkylinePreview(){
    //SkylineView()
}