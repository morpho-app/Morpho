package morpho.app.app


import dagger.Component
import kotlinx.coroutines.CoroutineScope

interface Supervisor {
  suspend fun CoroutineScope.onStart()
}
