package morpho.app.screens.postthread

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
import morpho.app.api.AtUri
import morpho.app.api.model.RecordUnion
import morpho.app.api.response.AtpResponse
import com.morpho.app.apiProvider
import morpho.app.base.BaseViewModel
import morpho.app.model.BskyPostThread
import morpho.app.model.toThread
import javax.inject.Inject

data class PostThreadState(
    val isLoading : Boolean = true,
    val isBlocked : Boolean = false,
    val notFound : Boolean = false,
    val currentUri: AtUri? = null,
)

@HiltViewModel
class PostThreadViewModel @Inject constructor(
    app: Application,
) : BaseViewModel(app), DefaultLifecycleObserver {
    val apiProvider = app.apiProvider

    var state by mutableStateOf(PostThreadState())
        private set

    var thread by mutableStateOf<BskyPostThread?>(null)


    fun createRecord(
        record: RecordUnion,
    ) = CoroutineScope(Dispatchers.Default).launch {
        apiProvider.createRecord(record)
    }


    fun loadThread(uri: AtUri, onComplete: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
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
                            notFound = true,

                        )
                    }
                    is GetPostThreadResponseThreadUnion.ThreadViewPost -> {
                        thread = threadResponse.value.toThread()
                        state = PostThreadState(false, currentUri = uri)
                    }
                }
                onComplete()
            }
        }
    }

}

