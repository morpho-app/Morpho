package radiant.nimbus.screens.notifications

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
fun NotificationsScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    BackHandler {
        navigator.navigateUp()
    }
    NotificationsView(navigator, navBar = {mainViewModel.navBar?.let {it(5)}})
}

@Composable
fun NotificationsView(
    navigator: DestinationsNavigator,
    navBar: @Composable() () -> Unit = {},
){
    ScreenBody(modifier = Modifier.fillMaxSize(), navBar = navBar) {

        Center {
            Text("Notifications Not Implemented Yet")
        }
    }
}

@Composable
@Preview(showBackground = true)
fun NotificationsPreview(){
    //NotificationsView()
}