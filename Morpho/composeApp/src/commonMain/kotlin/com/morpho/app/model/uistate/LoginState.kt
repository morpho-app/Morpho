package com.morpho.app.model.uistate

import androidx.compose.runtime.Immutable
import com.morpho.butterfly.auth.AuthInfo
import com.morpho.butterfly.auth.Credentials
import kotlinx.serialization.Serializable

@Immutable
@Serializable
enum class LoginScreenMode {
    SIGN_UP,
    SIGN_IN
}

@Immutable
@Serializable
sealed interface AuthState {
    data object NoAuth : AuthState

    @Immutable
    data class Success(val authInfo: AuthInfo) : AuthState

    @Immutable
    data class Error(val errorMessage: String) : AuthState
}

@Serializable
@Immutable
data class LoginState(
    val loadingState: UiLoadingState = UiLoadingState.Idle,
    val mode: LoginScreenMode = LoginScreenMode.SIGN_IN,
    val authState: AuthState = AuthState.NoAuth,
    val credentials: Credentials? = null,
)

