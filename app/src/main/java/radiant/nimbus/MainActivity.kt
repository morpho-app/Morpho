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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import app.bsky.actor.GetProfileQueryParams
import app.bsky.actor.toPreferences
import app.bsky.feed.GetFeedGeneratorQueryParams
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
import radiant.nimbus.screens.skyline.FeedTab
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
        var loggedIn = false
        WindowCompat.setDecorFitsSystemWindows(window, false)

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
            val credentials = viewModel.apiProvider.credentials().first()
            if(authInfo != null) {
                Log.i(TAG, "Auth Info: $authInfo")
                // Got around it (maybe) by adding a manual function to the api
                // that does the refresh with provided auth tokens
                when(val prefs = viewModel.apiProvider.getUserPreferences()) {
                    is AtpResponse.Failure -> {
                        Log.e(TAG, "Couldn't get Preferences: $prefs")
                        if(credentials != null) {
                            when (val response = viewModel.apiProvider.makeLoginRequest(credentials)) {
                                is AtpResponse.Failure -> {
                                    Log.e(TAG, "Login failure: $response")
                                }
                                is AtpResponse.Success -> {
                                    val p = viewModel.apiProvider.getUserPreferences().maybeResponse()
                                    Log.d(TAG, "Preferences load: $p")
                                    Log.i(TAG, "Using cached credentials for ${credentials.username}, going to home screen")
                                    viewModel.apiProvider.loginRepository.auth = response.response
                                    val profile = viewModel.apiProvider.api.getProfile(GetProfileQueryParams(AtIdentifier(credentials.username.handle)))
                                    loggedIn = true
                                    viewModel.currentUser = profile.requireResponse().toProfile()
                                    viewModel.userPreferences = p?.toPreferences()
                                }
                            }
                        } else {
                            Log.d(TAG, "No cached credentials, punting to login screen")
                            loggedIn = false
                        }
                    }
                    is AtpResponse.Success -> {
                        Log.d(TAG, "Preferences load successful: $prefs, going to home screen")
                        val profile = viewModel.apiProvider.api.getProfile(GetProfileQueryParams(AtIdentifier(authInfo.did.did)))
                        loggedIn = true
                        viewModel.currentUser = profile.requireResponse().toProfile()
                        viewModel.userPreferences = prefs.response.toPreferences()
                    }
                }
            } else {
                if(credentials != null) {
                    when (val response = viewModel.apiProvider.makeLoginRequest(credentials)) {
                        is AtpResponse.Failure -> {
                            Log.e(TAG, "Login failure: $response")
                        }
                        is AtpResponse.Success -> {
                            val p = viewModel.apiProvider.getUserPreferences().maybeResponse()
                            Log.d(TAG, "Preferences load: $p")
                            Log.i(TAG, "Using cached credentials for ${credentials.username}, going to home screen")
                            val profile = viewModel.apiProvider.api.getProfile(GetProfileQueryParams(AtIdentifier(credentials.username.handle)))
                            loggedIn = true
                            viewModel.currentUser = profile.requireResponse().toProfile()
                            viewModel.userPreferences = p?.toPreferences()
                        }
                    }
                } else {
                    Log.d(TAG, "No cached credentials, punting to login screen")
                    loggedIn = false
                }
            }
            viewModel.userPreferences?.savedFeeds?.pinned?.map { feedUri ->
                when(val response = viewModel.apiProvider.api.getFeedGenerator(
                    GetFeedGeneratorQueryParams(feedUri)
                )) {
                    is AtpResponse.Failure -> {
                        Log.e("Skyline", "Error getting feed info: $response")}
                    is AtpResponse.Success -> {
                        viewModel.pinnedFeeds += FeedTab(response.response.view.displayName, response.response.view.uri)
                    }
                }
            }
        }



        setContent {
            val engine = rememberAnimatedNavHostEngine()
            val navController = engine.rememberNavController()
            var selectedTab by rememberSaveable { mutableIntStateOf(0) }
            viewModel.windowSizeClass = calculateWindowSizeClass(this)
            viewModel.navBar = {
                NimbusNavigation(
                    navController = navController,
                    profilePic = { state, onClick ->
                        if(viewModel.currentUser != null) {
                            viewModel.currentUser!!.avatar?.let { avatar ->
                                OutlinedAvatar(url = avatar,
                                    modifier = Modifier.size(30.dp),
                                    onClicked = onClick,
                                    outlineSize = if (state) 2.dp else 0.dp,
                                    outlineColor = if(state) {
                                            TabRowDefaults.primaryContentColor
                                        } else {
                                            Color.Transparent
                                        },
                                    circle = true
                                    )
                            }
                        } else {
                            Icon(imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Profile")
                        }
                    },
                    actor = viewModel.currentUser?.did?.did?.let { AtIdentifier(it) },
                    selected = selectedTab,
                )
            }
            val startRoute = if (!loggedIn) LoginScreenDestination else SkylineScreenDestination
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