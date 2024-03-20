package com.morpho.app

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import app.bsky.actor.GetProfileQuery
import app.bsky.feed.GetFeedGeneratorQuery
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.navigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import com.morpho.butterfly.AtIdentifier
import com.morpho.app.model.toPreferences
import com.morpho.app.extensions.lifecycleViewModels
import com.morpho.app.model.DetailedProfile
import com.morpho.app.model.toProfile
import com.morpho.app.screens.NavGraphs
import com.morpho.app.screens.appCurrentDestinationAsState
import com.morpho.app.screens.destinations.LoginScreenDestination
import com.morpho.app.screens.destinations.SkylineScreenDestination
import com.morpho.app.screens.skyline.FeedTab
import com.morpho.app.ui.common.MorphoNavigation
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.theme.MorphoTheme
import com.morpho.butterfly.AtUri
import com.ramcosta.composedestinations.rememberNavHostEngine
import okhttp3.internal.wait


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
        var loggedIn by mutableStateOf(false)
        var waiting by mutableStateOf(true)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        var me: DetailedProfile? = null

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

            val authInfo = applicationContext.user.auth
            val credentials = applicationContext.user.credentials
            launch {
                if (authInfo != null) {
                    Log.i(TAG, "Auth Info: $authInfo")
                    viewModel.butterfly.getUserPreferences().onFailure { prefsFail ->
                        Log.e(TAG, "Couldn't get Preferences: $prefsFail")
                        if (credentials != null) {
                            viewModel.butterfly.makeLoginRequest(credentials)
                                .onFailure { loginFail ->
                                    Log.e(TAG, "Login failure: $loginFail")
                                }.onSuccess {
                                    launch {
                                        val p =
                                            viewModel.butterfly.getUserPreferences().getOrNull()
                                        Log.d(TAG, "Preferences load: $p")
                                        viewModel.userPreferences = p?.toPreferences()
                                    }
                                    Log.i(
                                        TAG,
                                        "Using cached credentials for ${credentials.username}, going to home screen"
                                    )
                                    val profile = viewModel.butterfly.api.getProfile(
                                        GetProfileQuery(AtIdentifier(credentials.username.handle))
                                    )
                                    loggedIn = true
                                    viewModel.currentUser = profile.getOrThrow().toProfile()
                                    me = viewModel.currentUser
                                }
                        }

                    }.onSuccess {
                        Log.d(TAG, "Preferences load successful: $it, going to home screen")
                        val profile = viewModel.butterfly.api.getProfile(
                            GetProfileQuery(
                                AtIdentifier(authInfo.did.did)
                            )
                        )
                        loggedIn = true
                        viewModel.currentUser = profile.getOrThrow().toProfile()
                        viewModel.userPreferences = it.toPreferences()
                        me = viewModel.currentUser
                    }

                } else {
                    if (credentials != null) {
                        viewModel.butterfly.makeLoginRequest(credentials).onFailure {
                            Log.e(TAG, "Login failure: $it")
                        }.onSuccess {
                            launch {
                                val p = viewModel.butterfly.getUserPreferences().getOrNull()
                                Log.d(TAG, "Preferences load: $p")
                                viewModel.userPreferences = p?.toPreferences()
                            }
                            Log.i(
                                TAG,
                                "Using cached credentials for ${credentials.username}, going to home screen"
                            )
                            val profile = viewModel.butterfly.api.getProfile(
                                GetProfileQuery(
                                    AtIdentifier(credentials.username.handle)
                                )
                            )
                            loggedIn = true
                            viewModel.currentUser = profile.getOrThrow().toProfile()
                            me = viewModel.currentUser
                        }
                    } else {
                        Log.d(TAG, "No cached credentials, punting to login screen")
                        loggedIn = false
                    }
                }
            }
        }
        runBlocking {
            viewModel.pinnedFeeds.add(FeedTab("Home", AtUri("__home__")))
            viewModel.userPreferences?.savedFeeds?.pinned?.forEachIndexed { index, feedUri ->
                launch {
                    viewModel.butterfly.api.getFeedGenerator(GetFeedGeneratorQuery(feedUri)).onFailure {
                        Log.e("Skyline", "Error getting feed info: $it")
                    }.onSuccess {
                        viewModel.pinnedFeeds.add(FeedTab(it.view.displayName, it.view.uri))
                    }
                }.invokeOnCompletion {
                    waiting = index == viewModel.pinnedFeeds.lastIndex
                    Log.i("Skyline", "waiting = $waiting")
                }

            }
        }
        Log.i("Main", "loggedIn = $loggedIn")
        Log.i("Main", "waiting = $waiting")

        if(!waiting) setContent {

            val engine = rememberNavHostEngine()
            val navController = engine.rememberNavController()
            val selectedTab by rememberSaveable { mutableIntStateOf(0) }
            viewModel.windowSizeClass = calculateWindowSizeClass(this)
            viewModel.navBar = {
                MorphoNavigation(
                    navController = navController,
                    profilePic = { state, onClick ->
                        if(me != null) {
                            me?.avatar?.let { avatar ->
                                OutlinedAvatar(url = avatar,
                                    modifier = Modifier.size(30.dp),
                                    onClicked = onClick,
                                    outlineSize = if (state) 2.dp else 0.dp,
                                    outlineColor = if(state) {
                                            TabRowDefaults.primaryContentColor
                                        } else {
                                            Color.Transparent
                                        },
                                    shape = AvatarShape.Rounded
                                    )
                            }
                        } else {
                            Icon(imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Profile")
                        }
                    },
                    selected = selectedTab,
                    viewModel = viewModel
                )
            }
            val startRoute = if (viewModel.currentUser == null) LoginScreenDestination else SkylineScreenDestination
            MorphoTheme(dynamicColor = true) {
                DestinationsNavHost(
                    engine = engine,
                    navGraph = NavGraphs.root,
                    navController = navController,
                    startRoute = startRoute
                )
                ShowLoginWhenLoggedOut(viewModel, navController)
                LaunchedEffect(Clock.System.now().epochSeconds % 60L == 0L) {
                    if (loggedIn) viewModel.getUnreadCount()
                }
                LaunchedEffect(loggedIn) {
                    if (loggedIn) navController.navigate(SkylineScreenDestination())
                }

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
        navController.navigate(LoginScreenDestination)
    }
}