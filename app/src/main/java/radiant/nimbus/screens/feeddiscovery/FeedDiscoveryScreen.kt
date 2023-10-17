package radiant.nimbus.screens.feeddiscovery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import radiant.nimbus.MainViewModel
import radiant.nimbus.components.Center
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel

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