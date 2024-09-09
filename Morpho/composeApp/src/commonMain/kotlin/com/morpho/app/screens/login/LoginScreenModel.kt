package com.morpho.app.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.morpho.app.model.uistate.AuthState
import com.morpho.app.model.uistate.LoginState
import com.morpho.app.model.uistate.UiLoadingState
import com.morpho.app.screens.base.BaseScreenModel
import com.morpho.app.util.checkValidUrl
import com.morpho.butterfly.Handle
import com.morpho.butterfly.auth.Credentials
import com.morpho.butterfly.auth.Server
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

class LoginScreenModel: BaseScreenModel() {
    var loginState: LoginState by mutableStateOf(LoginState())

    var email by mutableStateOf("")
    var handle by mutableStateOf("")
    var password by mutableStateOf("")
    var service by mutableStateOf("bsky.social")

    companion object {
        val log = logging()
    }

    fun onLoginClicked(handle: String) {
        val credentials = Credentials(email, Handle(handle), password, null)
        loginState = loginState.copy(
            loadingState = UiLoadingState.Loading,
            credentials = credentials
        )
        val server = if (service.contains("bsky.social")) Server.BlueskySocial else {
            service = "https://${service}"
            // If the url is fucked up, just try Bluesky
            if(checkValidUrl(service) != null) Server.CustomServer(service) else Server.BlueskySocial
        }
        viewModelScope.launch {
            api.makeLoginRequest(credentials, server).onSuccess {
                loginState = loginState.copy(
                    loadingState = UiLoadingState.Idle,
                    authState = AuthState.Success(it)
                )
            }.onFailure {
                loginState = loginState.copy(
                    loadingState = UiLoadingState.Idle,
                    authState = AuthState.Error(it.toString())
                )
            }
        }
    }

    fun onSignUpClicked() {

    }

    fun onServiceChanged(service: String) {
        this.service = service
    }

    fun onHandleChanged(maybeHandle: String) {
        if (maybeHandle.contains('@')) {
            email = maybeHandle
        } else {
            handle = maybeHandle
        }
    }

    fun onPasswordChanged(password: String) {
        this.password = password
    }
}