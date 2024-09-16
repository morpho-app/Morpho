package com.morpho.app.data

import com.morpho.app.myLang
import com.morpho.butterfly.ButterflyAgent
import com.morpho.butterfly.Language
import org.koin.core.component.inject

class MorphoAgent: ButterflyAgent() {
    val morphoPrefsRepo: PreferencesRepository by inject()
    val myLanguage = Language(myLang ?: "en") // TODO: make this configurable
}