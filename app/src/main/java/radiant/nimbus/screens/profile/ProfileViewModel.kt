package radiant.nimbus.screens.profile

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import app.bsky.actor.GetProfileQueryParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.base.BaseViewModel
import radiant.nimbus.model.DetailedProfile
import radiant.nimbus.model.toProfile
import sh.christian.ozone.api.AtIdentifier
import sh.christian.ozone.api.response.AtpResponse
import javax.inject.Inject


data class ProfileState(
    var actor: AtIdentifier? = null,
    var profile: DetailedProfile? = null,
    var isLoading: Boolean = true,
    var isError: Boolean = false,
) {
}




@HiltViewModel
class ProfileViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(ProfileState())
        private set


    fun getProfile(
        apiProvider: ApiProvider,
        actor: AtIdentifier,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) = viewModelScope.launch {
        when(val result = apiProvider.api.getProfile(GetProfileQueryParams(actor))) {
            is AtpResponse.Failure -> {
                Log.e("P Load Err", result.toString())
                state = ProfileState(actor, null, isLoading = false, isError = true)
                onFailure()
            }

            is AtpResponse.Success -> {
                val profile = result.response.toProfile()
                Log.e("P Load Success", result.toString())
                state = ProfileState(actor,profile,false)
                onSuccess()
            }
        }
    }
}