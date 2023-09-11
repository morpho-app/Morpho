package radiant.nimbus.app

import kotlinx.coroutines.CoroutineScope

interface Supervisor {
  suspend fun CoroutineScope.onStart()
}
