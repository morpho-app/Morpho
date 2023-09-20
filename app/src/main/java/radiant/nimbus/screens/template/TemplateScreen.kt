package radiant.nimbus.screens.template

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponse
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import radiant.nimbus.MainViewModel
import radiant.nimbus.components.Center
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.response.AtpResponse

@RootNavGraph(start = true)
@Destination
@Composable
fun TemplateScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: TemplateViewModel = hiltViewModel()
) {

    val params = GetPostThreadQueryParams(
        AtUri("""at://did:plc:5jb2734ccyccx6a3hjjuzhrr/app.bsky.feed.post/3k7pa2gm4rw2x"""), 12, 100)
    var postThread: AtpResponse<GetPostThreadResponse>? by remember { mutableStateOf(null)}
    LaunchedEffect(viewModel) {
        postThread = mainViewModel.apiProvider.api.getPostThread(params)
        Log.i("Thread", Json.encodeToString(postThread?.maybeResponse()?.thread.toString()))
    }
    Text(text = Json.encodeToString(postThread?.maybeResponse()?.thread.toString()))
    TemplateView("Please Sign In")
}

@Composable
fun TemplateView(text:String
){

    ScreenBody(modifier = Modifier.fillMaxSize()) {
        Center {
            Text(text)

        }
    }
}

@Composable
@Preview(showBackground = true)
fun TemplatePreview(){
    TemplateView("Test Text")
}