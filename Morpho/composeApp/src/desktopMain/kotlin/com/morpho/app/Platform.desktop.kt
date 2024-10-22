@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package com.morpho.app
import kotlinx.datetime.LocalDateTime
import net.harawata.appdirs.AppDirsFactory
import okio.Path.Companion.toPath
import java.util.Locale
import kotlin.io.path.createDirectories

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

actual fun getPlatformStorageDir(baseDir: String): String {
    val storageDir = AppDirsFactory.getInstance()
        .getUserDataDir(BuildKonfig.packageName, BuildKonfig.versionNumber, BuildKonfig.appName)
    val path = storageDir.toPath()
    path.toNioPath().createDirectories()
    return storageDir.toString()
}