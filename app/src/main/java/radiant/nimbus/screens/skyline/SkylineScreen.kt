package radiant.nimbus.screens.skyline

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import radiant.nimbus.MainViewModel
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel

@Destination
@Composable
fun SkylineScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: SkylineViewModel = hiltViewModel()
) {
    SkylineView()
}

@Composable
fun SkylineView(
){
    ScreenBody(modifier = Modifier.fillMaxSize()) {
    }
}

@Composable
@Preview(showBackground = true)
fun SkylinePreview(){
    SkylineView()
}