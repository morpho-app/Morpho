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
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.extensions.lifecycleViewModels
import radiant.nimbus.model.toProfile
import radiant.nimbus.screens.NavGraphs
import radiant.nimbus.screens.appCurrentDestinationAsState
import radiant.nimbus.screens.destinations.LoginScreenDestination
import radiant.nimbus.screens.destinations.SkylineScreenDestination
import radiant.nimbus.ui.common.NimbusNavigation
import radiant.nimbus.ui.elements.OutlinedAvatar
import radiant.nimbus.ui.theme.NimbusTheme


private const val TAG = "Main"
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val viewModel: MainViewModel by lifecycleViewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterialNavigationApi::class,
        ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var loggedin = false

        viewModel.supervisors.plus(viewModel.apiProvider)
        viewModel.supervisors.forEach { supervisor ->
            with(supervisor) {
                lifecycleScope.launch(SupervisorJob()) {
                    onStart()
                }
            }
        }
        /**
         * Authentication routine:
         *
         * Checks the disk cache for a JWT and tries a refresh
         * If there is none or the refresh fails for some reason, checks for cached user credentials
         * If those exist, it tries to use them to log in
         * If all of the above fail, it punts to the login screen
         *
         * TODO: Encrypt credentials at rest
         */
        runBlocking {
            val authInfo = viewModel.apiProvider.auth().first()
            if(authInfo != null) {
                Log.i(TAG, "Auth Info: $authInfo")
                // Got around it (maybe) by adding a manual function to the api
                // that does the refresh with provided auth tokens
                viewModel.apiProvider.loginRepository.auth = authInfo
                when(val refresh = viewModel.apiProvider.refreshSession(authInfo)) {
                    is AtpResponse.Failure -> {
                        Log.e(TAG, "Refresh failure: $refresh")
                        val credentials = viewModel.apiProvider.credentials().first()
                        if(credentials != null) {
                            when (val response = viewModel.apiProvider.makeLoginRequest(credentials)) {
                                is AtpResponse.Failure -> {
                                    Log.e(TAG, "Login failure: $response")
                                }
                                is AtpResponse.Success -> {
                                    Log.i(TAG, "Using cached credentials for ${credentials.username}, going to home screen")
                                    viewModel.apiProvider.loginRepository.auth = response.response
                                    val profile = viewModel.apiProvider.api.getProfile(GetProfileQueryParams(AtIdentifier(credentials.username.handle)))
                                    loggedin = true
                                    viewModel.currentUser = profile.requireResponse().toProfile()
                                }
                            }
                        } else {
                            Log.d(TAG, "No cached credentials, punting to login screen")
                            loggedin = false
                        }
                    }
                    is AtpResponse.Success -> {
                        Log.d(TAG, "Refresh Successful: $refresh, going to home screen")
                        val profile = viewModel.apiProvider.api.getProfile(GetProfileQueryParams(AtIdentifier(refresh.response.did.did)))
                        loggedin = true
                        viewModel.currentUser = profile.requireResponse().toProfile()
                    }
                }
            } else {
                val credentials = viewModel.apiProvider.credentials().first()
                if(credentials != null) {
                    when (val response = viewModel.apiProvider.makeLoginRequest(credentials)) {
                        is AtpResponse.Failure -> {
                            Log.e(TAG, "Login failure: $response")
                        }
                        is AtpResponse.Success -> {
                            Log.i(TAG, "Using cached credentials for ${credentials.username}, going to home screen")
                            val profile = viewModel.apiProvider.api.getProfile(GetProfileQueryParams(AtIdentifier(credentials.username.handle)))
                            loggedin = true
                            viewModel.currentUser = profile.requireResponse().toProfile()
                        }
                    }
                } else {
                    Log.d(TAG, "No cached credentials, punting to login screen")
                    loggedin = false
                }
            }
        }

        setContent {
            val engine = rememberAnimatedNavHostEngine()
            val navController = engine.rememberNavController()
            viewModel.windowSizeClass = calculateWindowSizeClass(this)
            viewModel.navBar = {
                NimbusNavigation(
                    navController = navController,
                    profilePic = {onClick ->
                        if(viewModel.currentUser != null) {
                            viewModel.currentUser!!.avatar?.let { avatar ->
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
            val startRoute = if (!loggedin) LoginScreenDestination else SkylineScreenDestination
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