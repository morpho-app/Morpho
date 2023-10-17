package radiant.nimbus.screens.template

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
import radiant.nimbus.api.AtUri
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel


@Destination
@Composable
fun TemplateScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: TemplateViewModel = hiltViewModel()
) {
    BackHandler {
        navigator.navigateUp()
    }
   TemplateView("Please Sign In")
}

@Composable
fun TemplateView(text:String
){

    ScreenBody(modifier = Modifier.fillMaxSize()) {
        //Center {
            Text(text)

        //}
    }
}

@Composable
@Preview(showBackground = true)
fun TemplatePreview(){

    //val testThreadData = GetPostThreadResponseThreadUnion.serializer().deserialize(testThreadJson)
    //TemplateView(testThreadData.toString())
}

val testThreadUri = AtUri("at://did:plc:uy7ehrx332p6dow5sk32hxzq/app.bsky.feed.post/3k7rmyigapw2y")