package radiant.nimbus.screens.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import radiant.nimbus.MainViewModel
import radiant.nimbus.components.Center
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import sh.christian.ozone.api.AtIdentifier

@RootNavGraph(start = true)
@Destination
@Composable
fun TemplateScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: TemplateViewModel = hiltViewModel()
) {
    navigator.navigate(ProfileScreenDestination(AtIdentifier("nonbinary.computer")))
    TemplateView()
}

@Composable
fun TemplateView(
){

    ScreenBody(modifier = Modifier.fillMaxSize()) {
        Center {
            Text("Template Screen")
        }
    }
}

@Composable
@Preview(showBackground = true)
fun TemplatePreview(){
    TemplateView()
}