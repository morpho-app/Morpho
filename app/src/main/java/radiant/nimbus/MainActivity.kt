package radiant.nimbus

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import app.bsky.actor.GetProfileQueryParams
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.rememberNavHostEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.Handle
import radiant.nimbus.api.auth.Credentials
import radiant.nimbus.extensions.lifecycleViewModels
import radiant.nimbus.model.toProfile
import radiant.nimbus.screens.NavGraphs
import radiant.nimbus.screens.appCurrentDestinationAsState
import radiant.nimbus.screens.destinations.LoginScreenDestination
import radiant.nimbus.screens.destinations.SkylineScreenDestination
import radiant.nimbus.ui.common.NimbusNavigation
import radiant.nimbus.ui.elements.OutlinedAvatar
import radiant.nimbus.ui.theme.NimbusTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: MainViewModel by lifecycleViewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterialNavigationApi::class,
        ExperimentalAnimationApi::class
    )
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

        val auth = runBlocking {
        viewModel.apiProvider.makeLoginRequest(
            /*Credentials(
                email = "aeiluindae@gmail.com",
                username = Handle("testenby.bsky.social"),
                password = "5hrz-kzs2-cgqg-v5jw",
                inviteCode = null
            )*/
            Credentials(
                email = "nat.neema.brown@gmail.com",
                username = Handle("nonbinary.computer"),
                password = "jn4c-borv-g3m7-4bea",
                inviteCode = null
            )
        )
        }
        viewModel.apiProvider.loginRepository.auth = auth.maybeResponse()
        Log.i("Auth", viewModel.apiProvider.loginRepository.auth.toString())
        val response = runBlocking {
            viewModel.apiProvider.api.getProfile(GetProfileQueryParams(AtIdentifier("nonbinary.computer")))
        }

        Log.i("Response", response.toString())
        viewModel.currentUser = response.requireResponse().toProfile()


        setContent {
            val engine = rememberNavHostEngine()
            val navController = engine.rememberNavController()
            viewModel.windowSizeClass = calculateWindowSizeClass(this)
            viewModel.navBar = {
                NimbusNavigation(
                    navController = navController,
                    profilePic = {onClick ->
                        if(viewModel.currentUser != null) {
                            response.requireResponse().avatar?.let { avatar ->
                                OutlinedAvatar(url = avatar,
                                    modifier = Modifier.size(30.dp),
                                    onClicked = onClick,
                                    )
                            }
                        } else {
                            Icon(imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Profile")
                        }
                    },
                    actor = viewModel.currentUser?.did?.did?.let { AtIdentifier(it) }
                )
            }
            val startRoute = if (response.maybeResponse() == null) LoginScreenDestination else SkylineScreenDestination
            NimbusTheme {


                DestinationsNavHost(
                    engine = engine,
                    navGraph = NavGraphs.root,
                    navController = navController,
                    startRoute = startRoute
                )
                ShowLoginWhenLoggedOut(viewModel, navController)

            }
        }
    }
}


@Composable
private fun ShowLoginWhenLoggedOut(
    vm: MainViewModel,
    navController: NavHostController
) {
    val currentDestination by navController.appCurrentDestinationAsState()
    val currentUser by remember { derivedStateOf { vm.currentUser } }

    if ((currentUser == null) && currentDestination != LoginScreenDestination) {
        // everytime destination changes or logged in state we check
        // if we have to show Login screen and navigate to it if so
        navController.navigate(LoginScreenDestination) {
        }
    }
}

val testThreadUri = AtUri("at://did:plc:mndtiksvxikpsy3zl6ebd2kr/app.bsky.feed.post/3k7rlrukr4w2v")
