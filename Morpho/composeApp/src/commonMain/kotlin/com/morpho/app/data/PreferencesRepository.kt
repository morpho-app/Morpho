package com.morpho.app.data

import app.bsky.actor.GetProfileQuery
import app.bsky.actor.PutPreferencesRequest
import com.morpho.app.model.bluesky.BskyPreferences
import com.morpho.app.model.bluesky.BskyUser
import com.morpho.app.model.bluesky.toPreferences
import com.morpho.app.model.bluesky.toProfile
import com.morpho.app.model.uistate.NotificationsFilterState
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.Butterfly
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.updatesOrEmpty
import io.github.xxfast.kstore.file.extensions.listStoreOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

@Serializable
data class BskyUserPreferences(
    val user: BskyUser,
    val preferences: BskyPreferences,
    val morphoPrefs: MorphoPreferences,
)

@Serializable
data class AccessibilityPreferences(
    val requireAltText: Boolean = false,
    val displayLargerAltBadge: Boolean = false,
    val reduceMotion: Boolean = false,
    val disableAutoplay: Boolean = false,
    val disableHaptics: Boolean = false,
)

@Serializable
data class MorphoPreferences(
    val tabbed: Boolean = true,
    val undecorated: Boolean = true,
    val notificationsFilter: NotificationsFilterState = NotificationsFilterState(),
    val accessibility: AccessibilityPreferences = AccessibilityPreferences(),
)

class PreferencesRepository(storageDir: String): KoinComponent {
    private val api: Butterfly by inject()

    private val _prefsStore: KStore<List<BskyUserPreferences>> = listStoreOf(
        file = "$storageDir/preferences.json".toPath(),
        enableCache = true
    )

    companion object {
        val log = logging()
    }

    val prefs: Flow<List<BskyUserPreferences>?>
        get() = _prefsStore.updatesOrEmpty.distinctUntilChanged()

    fun userPrefs(id: AtIdentifier): Flow<BskyUserPreferences?> = flow {
        prefs.onEach { preferencesList ->
            emit(preferencesList?.firstOrNull { p ->
                (p.user.userDid == id.toString()) || (p.user.handle == id.toString())
            })
        }
    }.distinctUntilChanged()



    //@NativeCoroutines
    suspend fun getPreferences(id: AtIdentifier, pullRemote: Boolean = false): Result<BskyPreferences> {
        val result: Result<BskyUserPreferences> = getFullPrefsLocal(id)
        val newPrefs = if (result.isSuccess && pullRemote) {
            val prefs = result.getOrNull()
            if (prefs != null) {
                pullPreferences(prefs.preferences)
            } else {
                pullPreferences(null)
            }
        } else if(pullRemote) {
            pullPreferences(null)
        } else {
            result.map { it.preferences }
        }.onSuccess {
            val user = getUser(id).getOrNull()
            if(result.isFailure) {
                val profile = api.api.getProfile(GetProfileQuery(id))
                    .getOrNull()?.toProfile()
                if(profile != null) {
                    setPreferences(BskyUser.makeUser(profile), it)
                }
            } else {
                setPreferences(user!!, it, result.getOrNull()!!.morphoPrefs)
            }
        }
        return newPrefs
    }

    suspend fun getFullPrefs(
        id: AtIdentifier, remote: Boolean = true
    ): Result<BskyUserPreferences> {
        return if (remote)  getFullPrefsRemote(id)
        else getFullPrefsLocal(id)
    }

    suspend fun getFullPrefsLocal(id: AtIdentifier): Result<BskyUserPreferences> {
        val prefs = prefs.firstOrNull()?.firstOrNull {
            (it.user.userDid == id.toString()) || (it.user.handle == id.toString())
        }
        return if (prefs != null) {
            Result.success(prefs)
        } else {
            Result.failure(Exception("No preferences found for user $id"))
        }
    }

    suspend fun getFullPrefsRemote(id: AtIdentifier): Result<BskyUserPreferences> {
        val prefs = prefs.firstOrNull()?.firstOrNull {
            (it.user.userDid == id.toString()) || (it.user.handle == id.toString())
        }
        return if (prefs != null) {
            pullPreferences(prefs.preferences).map {
                BskyUserPreferences(prefs.user, it, prefs.morphoPrefs)
            }
        } else {
            Result.failure(Exception("No preferences found for user $id"))
        }
    }

    suspend fun pullPreferences(p: BskyPreferences?): Result<BskyPreferences> {
        return if(p != null) api.api.getPreferences().map { it.toPreferences(p) }
        else api.api.getPreferences().map { it.toPreferences() }
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
    suspend fun setPreferences(user: BskyUser, pref: BskyPreferences, morphoPrefs: MorphoPreferences = MorphoPreferences()) = coroutineScope {
        _prefsStore.update {
            it?.toMutableList()?.apply {
                val prefsIndex = it.indexOfFirst { user.userDid == user.userDid }
                if (prefsIndex != -1) {
                    this[prefsIndex] = BskyUserPreferences(user, pref, morphoPrefs)
                } else {
                    add(BskyUserPreferences(user, pref, morphoPrefs))
                }
            }
        }
    }

    //@NativeCoroutines
    suspend fun setPreferencesRemote(user: BskyUser, pref: BskyPreferences, morphoPrefs: MorphoPreferences = MorphoPreferences()) = coroutineScope {
        setPreferences(user, pref, morphoPrefs)
        api.api.putPreferences(PutPreferencesRequest(pref.toRemotePrefs()))
    }
}