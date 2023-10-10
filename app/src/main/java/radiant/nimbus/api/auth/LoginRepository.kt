package radiant.nimbus.api.auth

import android.app.Application
import kotlinx.coroutines.flow.Flow
import radiant.nimbus.storage.getValue
import radiant.nimbus.storage.preference
import radiant.nimbus.storage.setValue
import radiant.nimbus.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

//@SingleInApp
@Singleton
class LoginRepository @Inject constructor(
    app: Application
) {
    private val authPreference = app.storage.preference<AuthInfo>("auth-info", null)

    private val credentialsPreference = app.storage.preference<Credentials>("credentials", null)

    var credentials by credentialsPreference
    fun credentials(): Flow<Credentials?> = credentialsPreference.updates

    var auth by authPreference
    fun auth(): Flow<AuthInfo?> = authPreference.updates
}