package radiant.nimbus.screens.settings

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
fun SettingsScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: SettingsViewModel = hiltViewModel()
) {
    BackHandler {
        navigator.navigateUp()
    }
    SettingsView()
}

@Composable
fun SettingsView(
){
    ScreenBody(modifier = Modifier.fillMaxSize()) {

        Center {
            Text("Settings Screen Not Implemented Yet")
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SettingsPreview(){
    SettingsView()
}