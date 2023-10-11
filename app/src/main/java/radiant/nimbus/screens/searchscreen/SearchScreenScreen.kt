package radiant.nimbus.screens.searchscreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import radiant.nimbus.MainViewModel
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun SearchScreenScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: SearchScreenViewModel = hiltViewModel()
) {
    SearchScreenView()
}

@Composable
fun SearchScreenView(
){
    ScreenBody(modifier = Modifier.fillMaxSize()) {
    }
}

@Composable
@Preview(showBackground = true)
fun SearchScreenPreview(){
    SearchScreenView()
}