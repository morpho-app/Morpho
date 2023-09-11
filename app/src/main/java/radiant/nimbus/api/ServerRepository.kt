package radiant.nimbus.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import radiant.nimbus.api.auth.AuthInfo

import radiant.nimbus.api.auth.Server
import radiant.nimbus.security.DataStoreUtil
import radiant.nimbus.storage.*
import radiant.nimbus.app.SingleInApp


@SingleInApp
class ServerRepository(
  storage: PersistentStorage,
) {
  private val serverPreference = storage.preference<Server>("servers", Server.BlueskySocial)

  var server by serverPreference
  fun server(): Flow<Server> = serverPreference.updates.filterNotNull()
}

