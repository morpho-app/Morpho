package com.morpho.app.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.morpho.app.CommonParcelable
import com.morpho.app.CommonParcelize
import com.morpho.app.model.uistate.AuthState
import com.morpho.app.screens.base.tabbed.TabbedBaseScreen
import com.morpho.app.ui.common.LoadingCircle
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import morpho.composeapp.generated.resources.BlueSkyKawaii
import morpho.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource

@CommonParcelize
@Serializable
data object LoginScreen: Tab, CommonParcelable {


    override val key: ScreenKey = hashCode().toString() + "TabbedLoginScreen"
    @Composable
    override fun Content() {


        val focusManager = LocalFocusManager.current
        val snackbarHostState = remember { SnackbarHostState() }
        val tabNavigator = LocalTabNavigator.current
        val screenModel = LocalNavigator.currentOrThrow.rememberNavigatorScreenModel { LoginScreenModel() }

        if(screenModel.isLoggedIn) {
            tabNavigator.current = TabbedBaseScreen
        }

        Scaffold (
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                when {
                    screenModel.loginState.authState is AuthState.Error -> {
                        Text(
                            text = "Error: ${(screenModel.loginState.authState as AuthState.Error).errorMessage}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    screenModel.loginState.authState is AuthState.NoAuth -> {
                        LoginView(
                            screenModel = screenModel,
                            focusManager = focusManager,
                            snackbarHostState = snackbarHostState,
                            innerPadding = innerPadding
                        )
                    }
                    screenModel.loginState.authState is AuthState.Success -> {
                        tabNavigator.current = TabbedBaseScreen
                    }
                    screenModel.loginState.isLoading && (screenModel.loginState.authState is AuthState.NoAuth) -> {
                        LoadingCircle()
                    }
                    else -> {
                        Text(
                            text = "Unknown Error",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

            }
        }

    }

    override val options: TabOptions
        @Composable get() = TabOptions(
                index = 1u,
                title = "Login",
            )

}

@Composable
fun SignupView(
    screenModel: LoginScreenModel,
    focusManager: FocusManager,
    snackbarHostState: SnackbarHostState,
    innerPadding: PaddingValues
) {
    var appPWOverride by rememberSaveable { mutableStateOf(false) }
    Text(
        text = "Sign up for Bluesky",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(innerPadding)
            .padding(top = 30.dp)
    )

    OutlinedTextField(
        value = screenModel.service,
        placeholder = { Text(text = "bsky.social") },
        label = { Text(text = "Service Provider") },
        onValueChange = {screenModel.onServiceChanged(it) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )

    OutlinedTextField(
        value = screenModel.handle,
        placeholder = { Text(text = "user.bsky.social") },
        label = { Text(text = "Handle or Email") },
        onValueChange = {
            screenModel.onHandleChanged(it)
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )

    OutlinedTextField(
        value = screenModel.password,
        placeholder = { Text(text = "App Password") },
        label = { Text(text = "Password") },
        onValueChange = {
            screenModel.onPasswordChanged(it)
        },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )

    Button(onClick = {
        if(screenModel.handle.isNotBlank() && screenModel.password.isNotBlank() && !isAppPassword(screenModel.password) && !appPWOverride) {
            screenModel.screenModelScope.launch {
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
                        screenModel.onLoginClicked(screenModel.handle)
                        focusManager.clearFocus()
                    }
                }
            }
        } else if (screenModel.handle.isNotBlank() && screenModel.password.isNotBlank() && (isAppPassword(screenModel.password) || appPWOverride)) {
            screenModel.onLoginClicked(screenModel.handle)
            focusManager.clearFocus()
        } else {
            screenModel.screenModelScope.launch {
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

@Composable
fun LoginView(
    screenModel: LoginScreenModel,
    focusManager: FocusManager,
    snackbarHostState: SnackbarHostState,
    innerPadding: PaddingValues
) {
    var appPWOverride by rememberSaveable { mutableStateOf(false) }

    val kawaiiMode = true

    Text(
        text = "Login to Bluesky",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(innerPadding)
            .padding(top = 30.dp)
    )

    OutlinedTextField(
        value = screenModel.service,
        placeholder = { Text(text = "bsky.social") },
        label = { Text(text = "Service Provider") },
        onValueChange = {screenModel.onServiceChanged(it) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )

    OutlinedTextField(
        value = screenModel.handle,
        placeholder = { Text(text = "user.bsky.social") },
        label = { Text(text = "Handle or Email") },
        onValueChange = {
            screenModel.onHandleChanged(it)
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )

    OutlinedTextField(
        value = screenModel.password,
        placeholder = { Text(text = "App Password") },
        label = { Text(text = "Password") },
        onValueChange = {
            screenModel.onPasswordChanged(it)
        },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )

    Button(onClick = {
        if(screenModel.handle.isNotBlank() && screenModel.password.isNotBlank() && !isAppPassword(screenModel.password) && !appPWOverride) {
            screenModel.screenModelScope.launch {
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
                        screenModel.onLoginClicked(screenModel.handle)
                        focusManager.clearFocus()
                    }
                }
            }
        } else if (screenModel.handle.isNotBlank() && screenModel.password.isNotBlank() && (isAppPassword(screenModel.password) || appPWOverride)) {
            screenModel.onLoginClicked(screenModel.handle)
            focusManager.clearFocus()
        } else {
            screenModel.screenModelScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Handle/Email or Password missing",
                    withDismissAction = true
                )
            }
        }
    }) {
        Text("Login")
    }

    if(kawaiiMode) {
        Image(
            painter = painterResource(Res.drawable.BlueSkyKawaii),
            contentDescription = "Bluesky Kawaii",
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
        )
    }
    Spacer(modifier = Modifier.height(80.dp))
}

fun isAppPassword(password: String) : Boolean {
    return !(password.matches(
        Regex.fromLiteral("^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}\$")))
}
