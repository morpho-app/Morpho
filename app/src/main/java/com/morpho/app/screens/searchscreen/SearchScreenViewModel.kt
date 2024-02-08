package morpho.app.screens.searchscreen

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import morpho.app.base.BaseViewModel
import javax.inject.Inject

data class SearchScreenState(
    val isLoading : Boolean = false
)

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(SearchScreenState())
        private set
}