package morpho.app.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.reflect.KClass

val DATA = stringPreferencesKey("data")
val SECURED_DATA = stringPreferencesKey("secured_data")

class DataStoreUtil
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
    private val security: SecurityUtil
) {
    private val securityKeyAlias = "data-store"
    private val bytesToStringSeparator = "|"


    fun getData(key: String) = dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey(key)].orEmpty()
        }

    suspend fun setData(key: String, value: String) {
        dataStore.edit {
            it[stringPreferencesKey("key")] = value
        }
    }

    fun getSecuredData(key: String) = dataStore.data
        .secureMap<String> { preferences ->
            preferences[stringPreferencesKey("enc_$key")].orEmpty()
        }

    suspend fun setSecuredData(key: String, value: String) {
        dataStore.secureEdit(value) { prefs, encryptedValue ->
            prefs[stringPreferencesKey("enc_$key")] = encryptedValue
        }
    }

    suspend fun hasKey(key: Preferences.Key<*>) = dataStore.edit { it.contains(key) }

    suspend fun clearDataStore() {
        dataStore.edit {
            it.clear()
        }
    }

    private val json: Json by lazy {
        Json { encodeDefaults = true }
    }

    private inline fun <reified T> Flow<Preferences>.secureMap(crossinline fetchValue: (value: Preferences) -> String): Flow<T> {
        return map {
            val decryptedValue = security.decryptData(
                securityKeyAlias,
                fetchValue(it).split(bytesToStringSeparator).map { it.toByte() }.toByteArray()
            )
            json.decodeFromString(decryptedValue)
        }
    }

    private suspend inline fun <reified T> DataStore<Preferences>.secureEdit(
        value: T,
        crossinline editStore: (MutablePreferences, String) -> Unit
    ) {
        edit {
            val encryptedValue = security.encryptData(securityKeyAlias, Json.encodeToString(value))
            editStore.invoke(it, encryptedValue.joinToString(bytesToStringSeparator))
        }
    }
}