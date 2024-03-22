package com.morpho.app.screens.login

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.auth.AuthInfo
import com.morpho.butterfly.auth.Credentials
import com.morpho.butterfly.auth.ServerInfo
import com.morpho.app.base.BaseViewModel
import javax.inject.Inject

enum class LoginScreenMode {
    SIGN_UP,
    SIGN_IN
}

sealed interface LoginState {
    val mode: LoginScreenMode
    val serverInfo: ServerInfo?

    data class ShowingLogin(
        override val mode: LoginScreenMode,
        override val serverInfo: ServerInfo?,
    ) : LoginState

    data class SigningIn(
        override val mode: LoginScreenMode,
        override val serverInfo: ServerInfo?,
        val credentials: Credentials,
    ) : LoginState

    data class ShowingError(
        override val mode: LoginScreenMode,
        override val serverInfo: ServerInfo?,
        val error: Throwable,
        val credentials: Credentials,
    ) : LoginState

    data class Success(
        override val mode: LoginScreenMode,
        override val serverInfo: ServerInfo?,
        val result: AuthInfo,
    ) : LoginState
}

data class LoginUIState(
    val isLoading: Boolean = false,
    var state: LoginState = LoginState.ShowingLogin(
        mode = LoginScreenMode.SIGN_IN,
        serverInfo = null
    )
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(LoginUIState())
        private set



    fun login(
        apiProvider: Butterfly,
        credentials: Credentials,
        onSuccess: (AuthInfo) -> Unit,
        onFailure: (Throwable) -> Unit
    ) = viewModelScope.launch {
        apiProvider.makeLoginRequest(credentials).onFailure {
                Log.e("Login error", it.toString())
                state.state = LoginState.ShowingError(
                    mode = state.state.mode,
                    serverInfo = state.state.serverInfo,
                    error = it,
                    credentials = credentials
                )
                onFailure(it)
            }
            .onSuccess {
                Log.i("Login success", it.toString())
                state.state = LoginState.Success(
                    mode = state.state.mode,
                    serverInfo = state.state.serverInfo,
                    result = it
                )
                onSuccess(it)
            }
    }
}
