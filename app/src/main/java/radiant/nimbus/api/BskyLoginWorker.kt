package radiant.nimbus.api

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.atproto.server.CreateSessionRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import radiant.nimbus.api.auth.AuthInfo
import radiant.nimbus.api.auth.Credentials
import sh.christian.ozone.api.response.AtpResponse

@HiltWorker
class BskyLoginWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters
): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val map = inputData.keyValueMap
            val apiProvider: ApiProvider? = map[API] as ApiProvider?
            val credentials: Credentials? = map[CREDENTIALS] as Credentials?
            if (credentials != null && apiProvider != null) {
                val request = CreateSessionRequest(credentials.username.handle, credentials.password)
                val response = apiProvider.api.createSession(request).map { response ->
                    AuthInfo(
                        accessJwt = response.accessJwt,
                        refreshJwt = response.refreshJwt,
                        handle = response.handle,
                        did = response.did,
                    )
                }
                when (response) {
                    is AtpResponse.Failure -> {
                        val outputData = workDataOf(
                            ERROR_INFO to response.error,
                            HEADERS to response.headers
                            )
                        Result.failure(outputData)
                    }
                    is AtpResponse.Success -> {
                        val authInfo = response.response
                        Result.success(workDataOf(AUTH_INFO to authInfo))
                    }
                }
            } else {
                Log.e(TAG, "Empty handle or password")
                Result.failure()
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Login Error", ex)
            Result.failure(workDataOf(ERROR_INFO to ex))
        }
    }

    companion object {
        private const val TAG = "BskyLoginWorker"
        const val API = "API"
        const val CREDENTIALS = "CREDENTIALS"
        const val AUTH_INFO = "AUTH_INFO"
        const val ERROR_INFO = "ERROR_INFO"
        const val HEADERS = "HEADERS"
    }
}