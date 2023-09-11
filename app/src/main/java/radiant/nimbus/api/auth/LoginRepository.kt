package radiant.nimbus.api.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import radiant.nimbus.security.DataStoreUtil
import kotlinx.serialization.json.Json
import radiant.nimbus.app.SingleInApp
import radiant.nimbus.storage.PersistentStorage
import radiant.nimbus.storage.getValue
import radiant.nimbus.storage.preference
import radiant.nimbus.storage.setValue

@SingleInApp
class LoginRepository(
    storage: PersistentStorage,
) {
    private val authPreference = storage.preference<AuthInfo>("auth-info", null)

    var auth by authPreference
    fun auth(): Flow<AuthInfo?> = authPreference.updates
}