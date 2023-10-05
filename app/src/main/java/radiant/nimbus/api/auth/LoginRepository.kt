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

    var auth by authPreference
    fun auth(): Flow<AuthInfo?> = authPreference.updates
}