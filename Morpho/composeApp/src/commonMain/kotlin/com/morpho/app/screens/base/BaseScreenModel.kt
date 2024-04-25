package com.morpho.app.screens.base

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.morpho.app.data.PreferencesRepository
import com.morpho.butterfly.AtIdentifier
import com.morpho.butterfly.AtUri
import com.morpho.butterfly.Butterfly
import com.morpho.butterfly.model.RecordType
import com.morpho.butterfly.model.RecordUnion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

open class BaseScreenModel : ScreenModel, KoinComponent {
    val api: Butterfly by inject()
    val preferences: PreferencesRepository by inject()

    val isLoggedIn: Boolean
        get() = api.isLoggedIn()

    companion object {
        val log = logging()
    }

    fun onProfileClicked(actor: AtIdentifier, tabbed: Boolean) {
        if (tabbed) {
            TODO("Navigate to the profile")
        } else {
            TODO("Create new card for the profile")
        }
    }

    fun onItemClicked(uri: AtUri, tabbed: Boolean) {
        if (tabbed) {
            TODO("Navigate to the thread/post/whatever")
        } else {
            TODO("Create new card for the thread")
        }
    }

    fun createRecord(record: RecordUnion) = screenModelScope.launch(Dispatchers.IO) {
        api.createRecord(record)
    }

    fun deleteRecord(type: RecordType, rkey: AtUri) = screenModelScope.launch(Dispatchers.IO) {
        api.deleteRecord(type, rkey)
    }


}