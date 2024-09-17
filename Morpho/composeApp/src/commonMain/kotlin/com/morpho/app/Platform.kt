@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
package com.morpho.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect val myLang:String?
expect val myCountry:String?


// For Android @Parcelize
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.TYPE)
expect annotation class CommonRawValue()
