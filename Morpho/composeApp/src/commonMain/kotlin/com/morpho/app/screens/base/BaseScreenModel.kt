package com.morpho.app.screens.base

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.data.PreferencesRepository
import com.morpho.app.model.bluesky.BskyPost
import com.morpho.app.model.uidata.BskyNotificationService
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.model.RecordType
import com.morpho.butterfly.model.RecordUnion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

open class BaseScreenModel : ScreenModel, KoinComponent {
    val api: Butterfly by inject()
    val preferences: PreferencesRepository by inject()
    val notifService: BskyNotificationService by inject()

    val isLoggedIn: Boolean
        get() = api.isLoggedIn()

    companion object {
        val log = logging()
    }

    fun createRecord(record: RecordUnion) = screenModelScope.launch(Dispatchers.IO) {
        api.createRecord(record)
    }

    fun deleteRecord(type: RecordType, rkey: AtUri) = screenModelScope.launch(Dispatchers.IO) {
        api.deleteRecord(type, rkey)
    }

    suspend fun getPost(uri: AtUri): BskyPost? {
        return com.morpho.app.model.uidata.getPost(uri, api).firstOrNull()
    }


}