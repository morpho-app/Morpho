package morpho.app.api.auth

import android.app.Application
import kotlinx.coroutines.flow.Flow
import morpho.app.storage.PersistentStorage
import morpho.app.storage.getValue
import morpho.app.storage.preference
import morpho.app.storage.setValue
import morpho.app.storage.storage

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