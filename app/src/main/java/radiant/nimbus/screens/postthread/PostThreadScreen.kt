package radiant.nimbus.screens.postthread

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.AtUri
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.model.BskyPostThread
import radiant.nimbus.ui.common.ThreadFragmentFrame

@Destination
@Composable
fun PostThreadScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: PostThreadViewModel = hiltViewModel(),
    uri: AtUri,
) {
    when {
        viewModel.state.isLoading -> {
            viewModel.loadThread(uri, mainViewModel.apiProvider)
        }
        !viewModel.state.isLoading -> {
            if (!viewModel.state.isBlocked || !viewModel.state.notFound || viewModel.thread != null ) {
                viewModel.thread?.let { PostThreadView(it, navBar = { mainViewModel.navBar?.let { it() } },) }
            }
        }
    }

}

@Composable
fun PostThreadView(
    thread: BskyPostThread,
    navBar: @Composable () -> Unit = {},
){
    ScreenBody(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        navBar = navBar,
    ) {
        ThreadFragmentFrame(thread = thread)
    }
}

@Composable
@Preview(showBackground = true)
fun PostThreadPreview(){
    //PostThreadView()
}