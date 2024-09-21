package com.morpho.app.data

import com.morpho.app.myLang
import com.morpho.butterfly.ButterflyAgent
import com.morpho.butterfly.Language

class MorphoAgent: ButterflyAgent() {
    val myLanguage = Language(myLang ?: "en") // TODO: make this configurable

    val morphoPrefs: MorphoPreferences = MorphoPreferences(
        kawaiiMode = true
    )


    val kawaiiMode: Boolean
        get() = morphoPrefs.kawaiiMode


}