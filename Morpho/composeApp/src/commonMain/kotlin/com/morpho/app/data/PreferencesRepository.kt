package com.morpho.app.data

import app.bsky.actor.PreferencesUnion
import com.morpho.app.model.uistate.NotificationsFilterState
import com.morpho.butterfly.BskyPreferences
import com.morpho.butterfly.Did
import com.morpho.butterfly.Language
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.updatesOrEmpty
import io.github.xxfast.kstore.file.extensions.listStoreOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okio.Path.Companion.toPath
import org.koin.core.component.KoinComponent
import org.lighthousegames.logging.logging

@Serializable
data class BskyUserPreferences(
    val did: Did,
    val preferences: BskyPreferences,
    val morphoPrefs: MorphoPreferences,
)

@Serializable
data class AccessibilityPreferences(
    val requireAltText: Boolean? = false,
    val displayLargerAltBadge: Boolean? = false,
    val reduceMotion: Boolean? = false,
    val disableAutoplay: Boolean? = false,
    val disableHaptics: Boolean? = false,
    val simpleUI: Boolean? = false,
) {
    companion object {
        fun update(
            existing: AccessibilityPreferences,
            new: AccessibilityPreferences,
        ) : AccessibilityPreferences {
            return AccessibilityPreferences(
                requireAltText = new.requireAltText ?: existing.requireAltText,
                displayLargerAltBadge = new.displayLargerAltBadge ?: existing.displayLargerAltBadge,
                reduceMotion = new.reduceMotion ?: existing.reduceMotion,
                disableAutoplay = new.disableAutoplay ?: existing.disableAutoplay,
                disableHaptics = new.disableHaptics ?: existing.disableHaptics,
                simpleUI = new.simpleUI ?: existing.simpleUI,
            )
        }

        fun toUpdate(
            requireAltText: Boolean? = null,
            displayLargerAltBadge: Boolean? = null,
            reduceMotion: Boolean? = null,
            disableAutoplay: Boolean? = null,
            disableHaptics: Boolean? = null,
            simpleUI: Boolean? = null,
        ): AccessibilityPreferences {
            return AccessibilityPreferences(
                requireAltText = requireAltText,
                displayLargerAltBadge = displayLargerAltBadge,
                reduceMotion = reduceMotion,
                disableAutoplay = disableAutoplay,
                disableHaptics = disableHaptics,
                simpleUI = simpleUI,
            )
        }
    }
}

enum class DarkModeSetting {
    SYSTEM,
    LIGHT,
    DARK,
}
@Serializable
data class MorphoPreferences(
    val tabbed: Boolean? = true,
    val undecorated: Boolean? = true,
    val kawaiiMode: Boolean? = true,
    val uiLanguage: Language? = Language("en"),
    val darkMode: DarkModeSetting? = DarkModeSetting.SYSTEM,
    val notificationsFilter: NotificationsFilterPref? = NotificationsFilterPref(),
    val accessibility: AccessibilityPreferences? = AccessibilityPreferences(),
): PreferencesUnion.ButterflyPreference() {
    companion object {
        fun update(
            existing: MorphoPreferences,
            new: MorphoPreferences,
        ) : MorphoPreferences {
            return MorphoPreferences(
                tabbed = new.tabbed ?: existing.tabbed,
                undecorated = new.undecorated ?: existing.undecorated,
                kawaiiMode = new.kawaiiMode ?: existing.kawaiiMode,
                notificationsFilter = new.notificationsFilter ?: existing.notificationsFilter,
                accessibility = new.accessibility ?: existing.accessibility,
                darkMode = new.darkMode ?: existing.darkMode,
                uiLanguage = new.uiLanguage ?: existing.uiLanguage,
            )
        }

        fun toUpdate(
            tabbed: Boolean? = null,
            undecorated: Boolean? = null,
            kawaiiMode: Boolean? = null,
            notificationsFilter: NotificationsFilterPref? = null,
            accessibility: AccessibilityPreferences? = null,
            darkMode: DarkModeSetting? = null,
            uiLanguage: Language? = null,
        ): MorphoPreferences {
            return MorphoPreferences(
                tabbed = tabbed,
                undecorated = undecorated,
                kawaiiMode = kawaiiMode,
                notificationsFilter = notificationsFilter,
                accessibility = accessibility,
                darkMode = darkMode,
                uiLanguage = uiLanguage,
            )
        }
    }
}

@Serializable
data class NotificationsFilterPref(
    val showAlreadyRead: Boolean? = true,
    val showLikes:  Boolean? = true,
    val showReposts: Boolean? = true,
    val showFollows: Boolean? = true,
    val showMentions: Boolean? = true,
    val showQuotes: Boolean? = true,
    val showReplies: Boolean? = true,
) {
    companion object {
        fun update(
            existing: NotificationsFilterPref,
            new: NotificationsFilterPref,
        ) : NotificationsFilterPref {
            return NotificationsFilterPref(
                showAlreadyRead = new.showAlreadyRead ?: existing.showAlreadyRead,
                showLikes = new.showLikes ?: existing.showLikes,
                showReposts = new.showReposts ?: existing.showReposts,
                showFollows = new.showFollows ?: existing.showFollows,
                showMentions = new.showMentions ?: existing.showMentions,
                showQuotes = new.showQuotes ?: existing.showQuotes,
                showReplies = new.showReplies ?: existing.showReplies,
            )
        }

        fun toUpdate(
            showAlreadyRead: Boolean? = null,
            showLikes:  Boolean? = null,
            showReposts: Boolean? = null,
            showFollows: Boolean? = null,
            showMentions: Boolean? = null,
            showQuotes: Boolean? = null,
            showReplies: Boolean? = null,
        ): NotificationsFilterPref {
            return NotificationsFilterPref(
                showAlreadyRead = showAlreadyRead,
                showLikes = showLikes,
                showReposts = showReposts,
                showFollows = showFollows,
                showMentions = showMentions,
                showQuotes = showQuotes,
                showReplies = showReplies,
            )
        }
    }
    fun toNotificationsFilterState(): NotificationsFilterState {
        return NotificationsFilterState(
            showAlreadyRead = showAlreadyRead == true,
            showLikes = showLikes == true,
            showReposts = showReposts == true,
            showFollows = showFollows == true,
            showMentions = showMentions == true,
            showQuotes = showQuotes == true,
            showReplies = showReplies == true,
        )
    }
}


class PreferencesRepository(storageDir: String): KoinComponent {

    private val _prefsStore: KStore<List<BskyUserPreferences>> = listStoreOf(
        file = "$storageDir/preferences.json".toPath(),
        enableCache = true
    )

    val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        val log = logging()
    }

    val prefs: Flow<List<BskyUserPreferences>?>
        get() = _prefsStore.updatesOrEmpty.distinctUntilChanged()

    fun morphoPrefs(did: Did): Flow<MorphoPreferences?> = userPrefs(did).map {
        it?.morphoPrefs
    }

    fun userPrefs(did: Did): Flow<BskyUserPreferences?> = prefs.map {
        it?.firstOrNull { prefs -> prefs.did == did }
    }

    fun bskyPrefs(did: Did): Flow<BskyPreferences?> = userPrefs(did).map {
        it?.preferences
    }

    suspend fun setMorphoPrefs(did: Did, prefs: MorphoPreferences) {
        _prefsStore.update {
            it?.toMutableList()?.apply {
                val prefsIndex = it.indexOfFirst { user -> user.did == did }
                if (prefsIndex != -1) {
                    val currentPrefs = this[prefsIndex]
                    this[prefsIndex] = currentPrefs.copy(morphoPrefs = prefs)
                } else {
                    add(BskyUserPreferences(did, BskyPreferences(), prefs))
                }
            }
        }
    }

    fun writePreferences(prefs: BskyUserPreferences) {
        serviceScope.launch {
            _prefsStore.update {
                it?.toMutableList()?.apply {
                    val prefsIndex = it.indexOfFirst { user -> user.did == prefs.did }
                    if (prefsIndex != -1) {
                        this[prefsIndex] = prefs
                    } else {
                        add(prefs)
                    }
                }
            }
        }
    }

    suspend fun setBskyPreferences(did: Did, prefs: BskyPreferences) {
        _prefsStore.update {
            it?.toMutableList()?.apply {
                val prefsIndex = it.indexOfFirst { user -> user.did == did }
                if (prefsIndex != -1) {
                    val currentPrefs = this[prefsIndex]
                    this[prefsIndex] = currentPrefs.copy(preferences = prefs)
                } else {
                    add(BskyUserPreferences(did, prefs, MorphoPreferences()))
                }
            }
        }
    }
}