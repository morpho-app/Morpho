package radiant.nimbus

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import app.bsky.feed.GetPostsQueryParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import radiant.nimbus.api.auth.Credentials
import radiant.nimbus.extensions.lifecycleViewModels
import radiant.nimbus.model.SkylineItem
import radiant.nimbus.model.toPost
import radiant.nimbus.ui.common.ThreadFragmentFrame
import radiant.nimbus.ui.theme.NimbusTheme
import sh.christian.ozone.api.AtIdentifier
import sh.christian.ozone.api.AtUri
import sh.christian.ozone.api.Handle

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: MainViewModel by lifecycleViewModels()

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

        val authInfo = runBlocking {
            //
            viewModel.apiProvider.makeLoginRequest(
                Credentials(
                    email = "aeiluindae@gmail.com",
                    username = Handle("testenby.bsky.social"),
                    password = "5hrz-kzs2-cgqg-v5jw",
                    inviteCode = null
                )
            ).maybeResponse()
        }
        viewModel.apiProvider.loginRepository.auth = authInfo
        Log.i("Auth", authInfo.toString())
        val response = runBlocking {
            viewModel.apiProvider.loginRepository.auth().first()
        }
        viewModel.apiProvider.loginRepository.auth = response
        Log.i("Response", response.toString())

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


        setContent {
            NimbusTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    if (authInfo == null) {
                        //DestinationsNavHost(navGraph = NavGraphs.root, startRoute = LoginScreenDestination)

                    } else {
                        viewModel.currentUser = AtIdentifier(authInfo.did.did)
                        //DestinationsNavHost(navGraph = NavGraphs.root)//, startRoute = ProfileScreenDestination)
                    }

                    item.thread?.let { ThreadFragmentFrame(thread = it) }

                }
            }
        }
    }
}

val testThreadUri = AtUri("at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v")
//val testThreadUri = AtUri("at://did:plc:uy7ehrx332p6dow5sk32hxzq/app.bsky.feed.post/3k7rmyigapw2y")