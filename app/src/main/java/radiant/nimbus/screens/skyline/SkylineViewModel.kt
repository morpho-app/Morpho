package radiant.nimbus.screens.skyline

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import radiant.nimbus.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class SkylineState(
    val isLoading : Boolean = false
)

@HiltViewModel
class SkylineViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(SkylineState())
        private set
}