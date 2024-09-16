@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package com.morpho.app
import kotlinx.datetime.LocalDateTime
import java.util.Locale

// Note: no need to define CommonParcelize here (bc its @OptionalExpectation)
actual interface CommonParcelable  // not used on iOS

// Note: no need to define CommonTypeParceler<T,P : CommonParceler<in T>> here (bc its @OptionalExpectation)
actual interface CommonParceler<T> // not used on iOS
actual object LocalDateTimeParceler : CommonParceler<LocalDateTime> // not used on iOS

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