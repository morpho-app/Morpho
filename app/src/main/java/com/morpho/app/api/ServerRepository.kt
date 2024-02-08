package morpho.app.api

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import morpho.app.api.auth.Server
import morpho.app.storage.PersistentStorage
import morpho.app.storage.getValue
import morpho.app.storage.preference
import morpho.app.storage.setValue
import morpho.app.storage.storage


//@SingleInApp
//@Singleton
class ServerRepository constructor(
  storage: PersistentStorage
) {
  constructor(app: Application) : this(app.storage)

  private val serverPreference = storage.preference<Server>("servers", Server.BlueskySocial)

  var server by serverPreference
  fun server(): Flow<Server> = serverPreference.updates.filterNotNull()
}

