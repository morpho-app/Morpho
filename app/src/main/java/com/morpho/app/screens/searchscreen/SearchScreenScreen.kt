package morpho.app.screens.searchscreen

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
fun SearchScreenScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: SearchScreenViewModel = hiltViewModel()
) {
    BackHandler {
        navigator.navigateUp()
    }
    SearchScreenView(navigator, navBar = {mainViewModel.navBar?.let {it(5)}})
}

@Composable
fun SearchScreenView(
    navigator: DestinationsNavigator,
    navBar: @Composable() () -> Unit = {},
){
    ScreenBody(modifier = Modifier.fillMaxSize(), navBar = navBar) {

        Center {
            Text("Search Not Implemented Yet")
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SearchScreenPreview(){
    //SearchScreenView()
}