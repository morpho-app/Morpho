package radiant.nimbus.screens.login

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.auth.AuthInfo
import radiant.nimbus.api.auth.Credentials
import radiant.nimbus.api.auth.ServerInfo
import radiant.nimbus.base.BaseViewModel
import sh.christian.ozone.api.response.AtpResponse
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
        val error: AtpResponse.Failure<*>,
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
        apiProvider: ApiProvider,
        credentials: Credentials,
        onSuccess: (AuthInfo) -> Unit,
        onFailure: (AtpResponse.Failure<AuthInfo>) -> Unit
    ) = viewModelScope.launch {
        when (val result = apiProvider.makeLoginRequest(credentials)) {
            is AtpResponse.Failure -> {
                state.state = LoginState.ShowingError(
                    mode = state.state.mode,
                    serverInfo = state.state.serverInfo,
                    error = result,
                    credentials = credentials
                )
                Log.e("Login error", result.toString())
                onFailure(result)
            }
            is AtpResponse.Success -> {
                state.state = LoginState.Success(
                    mode = state.state.mode,
                    serverInfo = state.state.serverInfo,
                    result = result.response
                )
                Log.i("Login success", result.toString())
                onSuccess(result.response)
            }
        }

    }

}
