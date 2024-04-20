package com.morpho.app.data

import app.bsky.actor.PutPreferencesRequest
import com.atproto.identity.ResolveHandleQuery
import com.morpho.app.model.bluesky.BskyPreferences
import com.morpho.app.model.bluesky.BskyUser
import com.morpho.app.model.bluesky.toPreferences
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.Handle
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.minus
import io.github.xxfast.kstore.extensions.plus
import io.github.xxfast.kstore.file.extensions.listStoreOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Serializable
data class BskyUserPreferences(
    val user: BskyUser,
    val preferences: BskyPreferences
)

class PreferencesRepository(storageDir: String): KoinComponent {
    private val api: Butterfly by inject()

    private val _prefsStore: KStore<List<BskyUserPreferences>> = listStoreOf(
        file = "$storageDir/preferences.json".toPath(),
        enableCache = true
    )

    private val prefs: Flow<List<BskyUserPreferences>?>
        get() = _prefsStore.updates.distinctUntilChanged()


    //@NativeCoroutines
    suspend fun getPreferences(id: AtIdentifier, pullRemote: Boolean = false): Result<BskyPreferences> {
        return getPrefsLocal(id).map { prefs ->
            if (pullRemote) {
                if(id == api.id) {
                    return@map api.api.getPreferences().map { it.toPreferences(prefs) }.getOrDefault(prefs)
                } else if(!id.toString().contains("did:") && !(api.id.toString().contains("did:"))) {
                    val did = api.api.resolveHandle(ResolveHandleQuery(Handle(id.toString()))).getOrNull()
                    val apiDid = api.api.resolveHandle(ResolveHandleQuery(Handle(api.id.toString()))).getOrNull()
                    if(did != null && apiDid != null && did == apiDid) {
                        return@map api.api.getPreferences().map { it.toPreferences(prefs) }.getOrDefault(prefs)
                    } else {
                        return@map prefs
                    }
                } else if(!id.toString().contains("did:")){
                    val did = api.api.resolveHandle(ResolveHandleQuery(Handle(id.toString()))).getOrNull()?.did
                    if(did != null && api.id != null && did == api.id) {
                        return@map api.api.getPreferences().map { it.toPreferences(prefs) }.getOrDefault(prefs)
                    } else {
                        return@map prefs
                    }
                } else if(!(api.id.toString().contains("did:"))) {
                    val apiDid = api.api.resolveHandle(ResolveHandleQuery(Handle(api.id.toString()))).getOrNull()?.did
                    if(id == apiDid) {
                        return@map api.api.getPreferences().map { it.toPreferences(prefs) }.getOrDefault(prefs)
                    } else {
                        return@map prefs
                    }
                } else {
                    return@map prefs
                }
            } else {
                return@map prefs
            }
        }
    }


    //@NativeCoroutines
    suspend fun getPrefsLocal(id: AtIdentifier): Result<BskyPreferences>  {
        val prefs = prefs.firstOrNull()?.firstOrNull {
            (it.user.userDid == id.toString()) || (it.user.handle == id.toString())
        }
        return if (prefs != null) {
            Result.success(prefs.preferences)
        } else {
            Result.failure(Exception("No preferences found for user $id"))
        }
    }

    //@NativeCoroutines
    suspend fun getUser(id: AtIdentifier): Result<BskyUser> {
        val user = prefs.firstOrNull()?.firstOrNull {
            (it.user.userDid == id.toString()) || (it.user.handle == id.toString())
        }?.user
        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(Exception("No user found for id $id"))
        }
    }

    //@NativeCoroutines
    suspend fun setPreferences(user: BskyUser, pref: BskyPreferences) = coroutineScope {
        val p =  this@PreferencesRepository.prefs.first()
        val prefsIndex = p?.indexOfFirst { it.user.userDid == user.userDid }
        if (prefsIndex != -1 && prefsIndex != null) {
            _prefsStore.minus(p[prefsIndex])
        }
        _prefsStore.plus(BskyUserPreferences(user, pref))
    }

    //@NativeCoroutines
    suspend fun setPreferencesRemote(user: BskyUser, pref: BskyPreferences) = coroutineScope {
        setPreferences(user, pref)
        api.api.putPreferences(PutPreferencesRequest(pref.toRemotePrefs()))
    }
}