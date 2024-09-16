package com.morpho.app

import java.util.Locale

actual val myLang:String?
    get() = Locale.getDefault().language

actual val myCountry:String?
    get() = Locale.getDefault().country