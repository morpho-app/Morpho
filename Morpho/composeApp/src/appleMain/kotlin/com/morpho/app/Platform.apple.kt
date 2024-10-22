package com.morpho.app

// For Android @Parcelize
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
actual annotation class CommonRawValue

actual val myLang:String?
    get() = NSLocale.currentLocale.languageCode

actual val myCountry:String?
    get() = NSLocale.currentLocale.countryCode

actual fun getPlatformStorageDir(baseDir: String): String {
    TODO("Not yet implemented")
}