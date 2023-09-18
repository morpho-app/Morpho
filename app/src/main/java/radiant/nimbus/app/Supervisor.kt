package radiant.nimbus.app


import dagger.Component
import kotlinx.coroutines.CoroutineScope

interface Supervisor {
  suspend fun CoroutineScope.onStart()
}
