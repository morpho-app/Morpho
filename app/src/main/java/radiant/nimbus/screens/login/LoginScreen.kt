package radiant.nimbus.screens.login

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
import androidx.hilt.navigation.compose.hiltViewModel
import app.bsky.actor.GetProfileQueryParams
import app.bsky.actor.toPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import radiant.nimbus.MainViewModel
import radiant.nimbus.api.AtIdentifier
import radiant.nimbus.api.Handle
import radiant.nimbus.api.auth.Credentials
import radiant.nimbus.components.ScreenBody
import radiant.nimbus.extensions.activityViewModel
import radiant.nimbus.model.toProfile
import radiant.nimbus.screens.destinations.SkylineScreenDestination

@Destination
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator,
    mainViewModel: MainViewModel = activityViewModel(),
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by rememberSaveable {mutableStateOf("") }
    var handle by rememberSaveable {mutableStateOf("") }
    var password by rememberSaveable {mutableStateOf("") }
    var service by rememberSaveable {mutableStateOf("bsky.social") }
    val loginState = viewModel.state.state

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
                                mainViewModel.apiProvider,
                                Credentials(
                                    email,
                                    Handle(handle),
                                    password,
                                    null
                                ),
                                {
                                    runBlocking {
                                        mainViewModel.currentUser = mainViewModel.apiProvider.api.getProfile(
                                            GetProfileQueryParams(AtIdentifier(handle))
                                        ).maybeResponse()?.toProfile()
                                        mainViewModel.userPreferences = mainViewModel.apiProvider.getUserPreferences()
                                            .maybeResponse()?.toPreferences()
                                    }


                                    navigator.navigate(
                                        SkylineScreenDestination
                                    )
                                },
                                {}
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
                mainViewModel.apiProvider,
                (loginState as LoginState.SigningIn).credentials,
                {
                    navigator.navigate(
                        SkylineScreenDestination
                    )
                },
                {}
            )
        }
        is LoginState.Success -> {
            navigator.navigate(
                SkylineScreenDestination
            )
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