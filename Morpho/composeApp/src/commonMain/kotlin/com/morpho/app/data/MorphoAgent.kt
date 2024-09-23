package com.morpho.app.data

import app.bsky.actor.AdultContentPref
import app.bsky.actor.PreferencesUnion
import app.bsky.labeler.LabelerViewDetailed
import com.morpho.app.myLang
import com.morpho.app.util.morphoSerializersModule
import com.morpho.butterfly.BskyPreferences
import com.morpho.butterfly.ButterflyAgent
import com.morpho.butterfly.InterpretedLabelDefinition
import com.morpho.butterfly.LabelValueID
import com.morpho.butterfly.LabelerID
import com.morpho.butterfly.Language
import com.morpho.butterfly.localize
import com.morpho.butterfly.xrpc.XrpcBlueskyApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.inject

class MorphoAgent: ButterflyAgent() {
    val localPrefs: PreferencesRepository by inject()

    val morphoPrefs: MutableStateFlow<MorphoPreferences> = MutableStateFlow(MorphoPreferences(
        kawaiiMode = true,
        notificationsFilter = NotificationsFilterPref(),
        accessibility = AccessibilityPreferences(),
    ))
    val bskyPrefs: MutableStateFlow<BskyPreferences> = MutableStateFlow(prefs)
    val myLanguage = MutableStateFlow(Language(myLang ?: morphoPrefs.value.uiLanguage?.tag ?: "en"))

    val labelersDetailed: Flow<List<LabelerViewDetailed>> = flow {
        val labelers = getLabelersDetailed(labelers).getOrNull() ?: listOf()
        emit(labelers)
    }

    init {
        // Belt and suspenders bc of the super/derived class initialization uncertainty
        api = XrpcBlueskyApi(atpClient, morphoSerializersModule)
        if(id != null) {
            runBlocking {
                getPreferences()
            }
            serviceScope.launch {
                localPrefs.morphoPrefs(id!!).distinctUntilChanged().collectLatest {
                    if (it != null && it != MorphoPreferences()) {
                        morphoPrefs.value = it
                    }
                }
            }
            serviceScope.launch {
                localPrefs.bskyPrefs(id!!).distinctUntilChanged().collectLatest {
                    if (it != null && it != BskyPreferences()) {
                        prefs = it
                    }
                }
            }
            serviceScope.launch {
                localPrefs.bskyPrefs(id!!).distinctUntilChanged().collectLatest {
                    if (it != null && it != BskyPreferences()) {
                        bskyPrefs.value = it
                    }
                }
            }
            serviceScope.launch {
                localPrefs.writePreferences(
                    BskyUserPreferences(
                        id!!,
                        prefs,
                        morphoPrefs.value
                    )
                )
            }
        }
    }


    val kawaiiMode: Boolean
        get() = morphoPrefs.value.kawaiiMode == true


    fun setAccessibilityPrefs(prefs: AccessibilityPreferences) = serviceScope.launch {
        updateMorphoPrefs {
            val newPrefs = AccessibilityPreferences.update(it.accessibility ?: AccessibilityPreferences(), prefs)
            it.copy(accessibility = newPrefs)
        }
    }
    fun setNotificationsFilterPrefs(prefs: NotificationsFilterPref) = serviceScope.launch {
        updateMorphoPrefs {
            val newPrefs = NotificationsFilterPref.update(it.notificationsFilter ?: NotificationsFilterPref(), prefs)
            it.copy(notificationsFilter = newPrefs)
        }
    }

    fun setDarkMode(setting: DarkModeSetting = DarkModeSetting.SYSTEM) = serviceScope.launch {
        updateMorphoPrefs {
            it.copy(darkMode = setting)
        }
    }

    fun setUILanguage(language: Language) = serviceScope.launch {
        myLanguage.value = language
        updateMorphoPrefs {
            it.copy(uiLanguage = language)
        }
    }
    suspend fun updateMorphoPrefs(
        updateFun: (MorphoPreferences) -> MorphoPreferences?
    ): Result<MorphoPreferences> {
        val prefs = updateFun(morphoPrefs.value)
        return if(prefs != null) {
            localPrefs.setMorphoPrefs(id!!, prefs)
            morphoPrefs.value = prefs
            Result.success(prefs)
        } else Result.failure(Exception("Update failed"))
    }

    suspend fun localizeLabelDefinitions(prefs: BskyPreferences): Map<LabelerID, Map<LabelValueID, InterpretedLabelDefinition>> {
        val labelDefs = getLabelDefinitions(prefs)
        return labelDefs.map {  labeler ->
            labeler.key to labeler.value.map { entry ->
                val labelDef = entry.value

                entry.key to labelDef.localize(myLanguage.value)
            }.associate { it.first to it.second }
        }.associate { it.first to it.second }
    }

    fun toggleAdultContent(enabled: Boolean) = serviceScope.launch {
        updatePreferences { prefs ->
            val newPref = if(enabled) AdultContentPref(true) else AdultContentPref(false)
            val updatedPrefs = prefs.filter { it !is PreferencesUnion.AdultContentPref }.plus(
                PreferencesUnion.AdultContentPref(newPref)
            )
            return@updatePreferences updatedPrefs
        }
    }


}