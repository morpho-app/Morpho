package morpho.app.screens.feedlist

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import morpho.app.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class FeedListState(
    val isLoading : Boolean = false
)

@HiltViewModel
class FeedListViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(FeedListState())
        private set
}