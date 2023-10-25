package radiant.nimbus.api.auth

import android.app.Application
import kotlinx.coroutines.flow.Flow
import radiant.nimbus.storage.PersistentStorage
import radiant.nimbus.storage.getValue
import radiant.nimbus.storage.preference
import radiant.nimbus.storage.setValue
import radiant.nimbus.storage.storage

//@SingleInApp
//@Singleton
class LoginRepository constructor(
    storage: PersistentStorage
) {
    constructor(app: Application) : this(app.storage)

    private val authPreference = storage.preference<AuthInfo>("auth-info", null)

    private val credentialsPreference = storage.preference<Credentials>("credentials", null)

    var credentials by credentialsPreference
    fun credentials(): Flow<Credentials?> = credentialsPreference.updates

    var auth by authPreference
    fun auth(): Flow<AuthInfo?> = authPreference.updates
}