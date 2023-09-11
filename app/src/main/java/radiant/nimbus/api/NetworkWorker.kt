package radiant.nimbus.api


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import sh.christian.ozone.api.response.AtpResponse

abstract class NetworkWorker<T> {
  fun run(): Flow<AtpResponse<T>> = flow {
    emit(execute())
  }.flowOn(Dispatchers.IO)

  abstract suspend fun execute(): AtpResponse<T>

  companion object {
    operator fun <T> invoke(
      block: suspend () -> AtpResponse<T>,
    ): NetworkWorker<T> = object : NetworkWorker<T>() {
      override suspend fun execute(): AtpResponse<T> = block()
    }
  }
}
