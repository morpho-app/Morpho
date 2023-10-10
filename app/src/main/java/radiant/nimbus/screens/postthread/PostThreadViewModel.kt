package radiant.nimbus.screens.postthread

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponseThreadUnion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import radiant.nimbus.api.ApiProvider
import radiant.nimbus.api.AtUri
import radiant.nimbus.api.model.RecordUnion
import radiant.nimbus.api.response.AtpResponse
import radiant.nimbus.base.BaseViewModel
import radiant.nimbus.model.BskyPostThread
import radiant.nimbus.model.toThread
import javax.inject.Inject

data class PostThreadState(
    val isLoading : Boolean = true,
    val isBlocked : Boolean = false,
    val notFound : Boolean = false,
)

@HiltViewModel
class PostThreadViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {

    var state by mutableStateOf(PostThreadState())
        private set

    var thread: BskyPostThread?  by mutableStateOf(null)
        private set

    fun createRecord(
        record: RecordUnion,
        apiProvider: ApiProvider,
    ) = CoroutineScope(Dispatchers.Default).launch {
        apiProvider.createRecord(record)
    }


    fun loadThread(uri: AtUri, apiProvider: ApiProvider, onComplete: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        when (val result = apiProvider.api.getPostThread(GetPostThreadQueryParams(uri))) {
            is AtpResponse.Failure -> {
                Log.e("Thread Load Err", result.toString())
                onComplete()
            }
            is AtpResponse.Success -> {
                when(val threadResponse = result.response.thread) {
                    is GetPostThreadResponseThreadUnion.BlockedPost -> {
                        state = PostThreadState(
                            isLoading = false,
                            isBlocked = true
                        )
                    }
                    is GetPostThreadResponseThreadUnion.NotFoundPost -> {
                        state = PostThreadState(
                            isLoading = false,
                            notFound = true
                        )
                    }
                    is GetPostThreadResponseThreadUnion.ThreadViewPost -> {
                        thread = threadResponse.value.toThread()
                        state = PostThreadState(true)
                    }
                }
                onComplete()
            }
        }
    }

}