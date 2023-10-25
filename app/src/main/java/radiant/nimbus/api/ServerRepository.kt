package radiant.nimbus.api

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import radiant.nimbus.api.auth.Server
import radiant.nimbus.storage.PersistentStorage
import radiant.nimbus.storage.getValue
import radiant.nimbus.storage.preference
import radiant.nimbus.storage.setValue
import radiant.nimbus.storage.storage


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

