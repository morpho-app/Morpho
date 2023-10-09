package radiant.nimbus.screens.skyline

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.feed.GetFeedQueryParams
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import radiant.nimbus.MainViewModel
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.screens.NavGraphs
import radiant.nimbus.screens.destinations.PostThreadScreenDestination
import radiant.nimbus.ui.common.SkylineFragment

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
        navBar = { mainViewModel.navBar?.let { it() } },
    )
}

@Composable
fun SkylineView(
    navigator: DestinationsNavigator,
    viewModel: SkylineViewModel,
    refresh: (String?) -> Unit = {},
    navBar: @Composable () -> Unit = {},
){

    ScreenBody(
        modifier = Modifier
            .fillMaxSize().systemBarsPadding()
            .heightIn(0.dp, 20000.dp),
        topContent = {},
        navBar = navBar

    ) {
        SkylineFragment(
            postFlow = viewModel.skylinePosts,
            onItemClicked = {
                navigator.navigate(PostThreadScreenDestination(it)) {
                    popUpTo(NavGraphs.root) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                            },
            refresh = refresh,
            modifier = Modifier
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SkylinePreview(){
    //SkylineView()
}