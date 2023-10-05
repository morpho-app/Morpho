package radiant.nimbus.screens.feeddiscovery

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import radiant.nimbus.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class FeedDiscoveryState(
    val isLoading : Boolean = false
)

@HiltViewModel
class FeedDiscoveryViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(FeedDiscoveryState())
        private set
}