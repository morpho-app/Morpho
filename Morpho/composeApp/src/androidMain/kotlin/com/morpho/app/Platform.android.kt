@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package com.morpho.app


import android.os.Build
import kotlinx.parcelize.RawValue
import java.util.Locale

actual typealias CommonRawValue = RawValue

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()




actual val myLang:String?
    get() = Locale.getDefault().language

actual val myCountry:String?
    get() = Locale.getDefault().country