@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package com.morpho.app
import java.util.Locale

// For Android @Parcelize
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
actual annotation class CommonRawValue


class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual val myCountry: String?
    get() = Locale.getDefault().country
actual val myLang: String?
    get() = Locale.getDefault().language