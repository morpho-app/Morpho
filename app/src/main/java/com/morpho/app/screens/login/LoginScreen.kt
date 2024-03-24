package com.morpho.app.screens.login

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.actor.GetProfileQuery
import app.bsky.feed.GetFeedGeneratorQuery
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.morpho.app.MainViewModel
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.Handle
import com.morpho.butterfly.auth.Credentials
import com.morpho.app.model.toPreferences
import com.morpho.app.components.ScreenBody
import com.morpho.app.extensions.activityViewModel
import com.morpho.app.model.toProfile
import com.morpho.app.screens.NavGraphs
import com.morpho.app.screens.destinations.LoginScreenDestination
import com.morpho.app.screens.destinations.SkylineScreenDestination
import com.morpho.app.screens.skyline.FeedTab
import com.morpho.butterfly.AtUri
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.coroutines.joinAll

@Destination
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: LoginViewModel = hiltViewModel()
) {
    BackHandler(true) { /* We want to disable back clicks */ }
    var email by rememberSaveable {mutableStateOf("") }
    var handle by rememberSaveable {mutableStateOf("") }
    var password by rememberSaveable {mutableStateOf("") }
    var service by rememberSaveable {mutableStateOf("bsky.social") }
    val loginState = viewModel.state.state
    val hasNavigatedUp = remember { mutableStateOf(false) }
    if (mainViewModel.currentUser != null) {
        hasNavigatedUp.value = true // avoids double navigation

        if (!navigator.navigateUp()) {
            // Sometimes we are starting on LoginScreen (to avoid UI jumps)
            // In those cases, navigateUp fails, so we just navigate to the registered start destination
            navigator.navigate(NavGraphs.root.startDestination as DirectionDestinationSpec) {
                popUpTo(LoginScreenDestination) {
                    inclusive = true
                }
            }
        }
    }
    else {
        when (viewModel.state.state) {
            is LoginState.ShowingError -> {

            }
            is LoginState.ShowingLogin -> {
                when (loginState.mode) {
                    LoginScreenMode.SIGN_UP -> TODO()
                    LoginScreenMode.SIGN_IN -> {
                        LoginView(
                            service,
                            handle,
                            password,
                            onLoginClick = {
                                viewModel.login(
                                    credentials = Credentials(
                                        email,
                                        Handle(handle),
                                        password,
                                        null
                                    ),
                                    onSuccess = {
                                        runBlocking {
                                            mainViewModel.pinnedFeeds.clear()
                                            mainViewModel.pinnedFeeds.add(FeedTab("Home", AtUri("__home__")))
                                            val tabs: MutableMap<Int, FeedTab> = mutableMapOf()
                                            launch {
                                                mainViewModel.currentUser = mainViewModel.butterfly.api.getProfile(
                                                    GetProfileQuery(it.did)
                                                ).getOrNull()?.toProfile()
                                            }
                                            launch {
                                                mainViewModel.userPreferences = mainViewModel.butterfly.getUserPreferences()
                                                    .getOrNull()?.toPreferences()
                                            }.invokeOnCompletion {
                                                launch {
                                                    mainViewModel.userPreferences?.savedFeeds?.pinned?.fastForEachIndexed { index, feedUri ->
                                                        launch {
                                                            mainViewModel.butterfly.api.getFeedGenerator(
                                                                GetFeedGeneratorQuery(feedUri)
                                                            ).onFailure {
                                                                Log.e("Skyline", "Error getting feed info: $it")
                                                            }.onSuccess {
                                                                tabs[index+1] = FeedTab(it.view.displayName, it.view.uri)
                                                            }
                                                        }
                                                    }
                                                }.invokeOnCompletion {
                                                    tabs.toSortedMap().forEach { entry-> mainViewModel.pinnedFeeds.add(entry.value) }
                                                }
                                            }

                                        }

                                        navigator.navigate(
                                            SkylineScreenDestination()
                                        ){
                                            popUpTo(NavGraphs.root) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onFailure = {}
                                )
                            },
                            onServiceChange = { service = it },
                            onHandleChange = {
                                if (it.contains('@')) {
                                    email = it
                                } else {
                                    handle = it
                                }
                            },
                            onPasswordChange = {
                                password = it

                            }
                        )
                    }
                }
            }
            is LoginState.SigningIn -> {
                viewModel.login(
                    (loginState as LoginState.SigningIn).credentials,
                    {
                        navigator.navigate(
                            SkylineScreenDestination()
                        ){
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    {}
                )
            }
            is LoginState.Success -> {
                navigator.navigate(
                    SkylineScreenDestination()
                ){
                    popUpTo(NavGraphs.root) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }


}


@Composable
fun LoginView(
    service: String,
    handle: String,
    password: String,
    onServiceChange: (String) -> Unit,
    onHandleChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: (String) -> Unit
){
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var appPWOverride by rememberSaveable { mutableStateOf(false) }

    ScreenBody(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Scaffold (
            snackbarHost = { SnackbarHost(snackbarHostState) },
            ) {
                innerPadding ->
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Login to Bluesky",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(top = 30.dp)
                )

                OutlinedTextField(
                    value = service,
                    placeholder = { Text(text = "bsky.social") },
                    label = { Text(text = "Service Provider") },
                    onValueChange = onServiceChange,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                OutlinedTextField(
                    value = handle,
                    placeholder = { Text(text = "user.bsky.social") },
                    label = { Text(text = "Handle or Email") },
                    onValueChange = onHandleChange,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                OutlinedTextField(
                    value = password,
                    placeholder = { Text(text = "App Password") },
                    label = { Text(text = "Password") },
                    onValueChange = {
                        onPasswordChange(it)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                Button(onClick = {
                    if(handle.isNotBlank() && password.isNotBlank() && !isAppPassword(password) && !appPWOverride) {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Please Use an App Password",
                                actionLabel = "Security Sucks",
                                withDismissAction = true
                            )
                            when(result) {
                                SnackbarResult.Dismissed -> {
                                    // Make them actually click the "Security Sucks" button to do the dumb thing
                                }
                                SnackbarResult.ActionPerformed -> {
                                    appPWOverride = true
                                    // But we won't make them actually click the login button again
                                    onLoginClick(handle)
                                    focusManager.clearFocus()
                                }
                            }
                        }
                    } else if (handle.isNotBlank() && password.isNotBlank() && (isAppPassword(password) || appPWOverride)) {
                        onLoginClick(handle)
                        focusManager.clearFocus()
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Handle/Email or Password missing",
                                withDismissAction = true
                            )
                        }
                    }
                }) {
                    Text("Login")
                }
            }
        }

    }
}

fun isAppPassword(password: String) : Boolean {
    return !(password.matches(
        Regex.fromLiteral("^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}\$")))
}

@Composable
@Preview(showBackground = true)
fun LoginPreview(){
    var email by rememberSaveable {mutableStateOf("") }
    var handle by rememberSaveable {mutableStateOf("") }
    var password by rememberSaveable {mutableStateOf("") }
    var service by rememberSaveable {mutableStateOf("bsky.social") }
    LoginView(
        service,
        handle,
        password,
        onLoginClick = {
        },
        onServiceChange = {service = it},
        onHandleChange = {
            if (it.contains('@')) {
                email = it
            } else {
                handle = it
            }
        },
        onPasswordChange = { password = it }
    )
}