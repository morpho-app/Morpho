package radiant.nimbus

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import app.bsky.actor.GetProfileQueryParams
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import radiant.nimbus.extensions.lifecycleViewModels
import radiant.nimbus.model.toProfile
import radiant.nimbus.screens.NavGraphs
import radiant.nimbus.screens.destinations.ProfileScreenDestination
import radiant.nimbus.ui.theme.NimbusTheme
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: MainViewModel by lifecycleViewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.supervisors.plus(viewModel.apiProvider)
        viewModel.supervisors.forEach { supervisor ->
            with(supervisor) {
                lifecycleScope.launch(SupervisorJob()) {
                    onStart()
                }
            }
        }

        val response = runBlocking {
            viewModel.apiProvider.api.getProfile(GetProfileQueryParams(AtIdentifier("nonbinary.computer")))
        }
        //viewModel.apiProvider.loginRepository.auth = response
        Log.i("Response", response.toString())
        /*
        val post = runBlocking {
            viewModel.apiProvider.api.getPosts(GetPostsQueryParams(persistentListOf<AtUri>().add(0,testThreadUri)))
        }.requireResponse().posts[0]
        //Log.i("Post", post.toString())

        val item = SkylineItem(post.toPost(), null)
        runBlocking{
            item.postToThread(viewModel.apiProvider).join()
        }
        //Log.i("Thread", item.thread?.post.toString())
        //Log.i("ThreadParents", item.thread?.parents.toString())
        //Log.i("ThreadReplies", item.thread?.replies.toString())
        */

        setContent {
            viewModel.windowSizeClass = calculateWindowSizeClass(this)
            NimbusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    if (response.maybeResponse() == null) {
                        //DestinationsNavHost(navGraph = NavGraphs.root, startRoute = LoginScreenDestination)

                    } else {
                        viewModel.currentUser = response.requireResponse().toProfile()
                        DestinationsNavHost(navGraph = NavGraphs.root, startRoute = ProfileScreenDestination)
                    }

                    //item.thread?.let { ThreadFragmentFrame(thread = it) }

                }
            }
        }
    }
}

val testThreadUri = AtUri("at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v")
//val testThreadUri = AtUri("at://did:plc:uy7ehrx332p6dow5sk32hxzq/app.bsky.feed.post/3k7rmyigapw2y")