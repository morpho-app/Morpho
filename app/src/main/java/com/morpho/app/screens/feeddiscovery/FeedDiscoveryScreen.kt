package morpho.app.screens.feeddiscovery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.morpho.app.MainViewModel
import morpho.app.components.Center
import morpho.app.components.ScreenBody
import morpho.app.extensions.activityViewModel

@Destination
@Composable
fun FeedDiscoveryScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: FeedDiscoveryViewModel = hiltViewModel()
) {
    BackHandler {
        navigator.navigateUp()
    }
    FeedDiscoveryView()
}

@Composable
fun FeedDiscoveryView(
){
    ScreenBody(modifier = Modifier.fillMaxSize()) {
        Center {
            Text("Feed Discovery Not Implemented Yet")
        }
    }
}

@Composable
@Preview(showBackground = true)
fun FeedDiscoveryPreview(){
    FeedDiscoveryView()
}